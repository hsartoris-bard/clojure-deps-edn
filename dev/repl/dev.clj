(ns repl.dev
  "REPL for development"
  (:require
    [nrepl.cmdline :as nrepl]
    ;[nrepl.server :as server]
    [reply.main :as reply]))

(defonce ^:private nrepl-started? (atom false))

(defn- start-nrepl
  "Start an nREPL server, saving it in `nrepl-server`."
  []
  (println "Starting REPL")
  (future
    (nrepl/-main "-h" "127.0.0.1" "--middleware" "[\"cider.nrepl/cider-middleware\"]"))
  (reset! nrepl-started? true)
  (println "REPL started"))

(defn- get-port
  []
  (or (try (slurp ".nrepl-port") (catch Throwable _))
      (do (start-nrepl)
          (Thread/sleep 1000)
          (slurp ".nrepl-port"))))


(def default-opts
  {:color         true
   :history-file  ".nrepl-history"
   :host          "127.0.0.1"
   :port          0})

(defn -main
  "Connect to a running REPL if available (port in .nrepl-port), or start one
  if not found."
  [& _args]
  (->> (get-port)
       (str "127.0.0.1:")
       (assoc default-opts :attach)
       reply/launch-nrepl) ;; blocks
  (when @nrepl-started?
    (System/exit 0)) ;; ensure REPL is shutdown if started
  (shutdown-agents))
