(ns user
  (:require [figwheel-sidecar.repl-api :as ra]
            [figwheel-sidecar.system :as fs]))
;; This namespace is loaded automatically by nREPL

;; read project.clj to get build configs
(def profiles (->> "project.clj"
                   slurp
                   read-string
                   (drop-while #(not= % :profiles))
                   (apply hash-map)
                   :profiles))

(def cljs-builds (get-in profiles [:dev :cljsbuild :builds]))

(defn start-figwheel
  ([build-id] (start-figwheel build-id false))
  ([build-id repl?]
   (let [port (if (= build-id :android) 6991 6992)
         start-fig (if repl? ra/start-figwheel! fs/start-figwheel!)]
     (start-fig
       {:figwheel-options {:server-port port}
        :build-ids        [build-id]
        :all-builds       cljs-builds}))
   (when repl?
     (ra/cljs-repl))))

(defn start-figwheels []
  (start-figwheel :android)
  (start-figwheel :ios true))

(defn start-ios-fig []
  (start-figwheel :ios true))