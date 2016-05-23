(ns catlantis.api
  (:require [catlantis.api-routes :refer [api-routes]]
            [ajax.core :refer [GET POST PUT DELETE]]
            [bidi.bidi :as b]
            [schema.core :as s :include-macros true]
            [catlantis.utils :as u]
            [print.foo :as pf :include-macros true]
            [tubax.core :as tub]
            [clojure.walk :as w]
            [clojure.zip :as z]
            [medley.core :as m]
            [camel-snake-kebab.core :as cs :include-macros true]
            [catlantis.config :as cfg]))

(s/defschema QueryParams {s/Keyword s/Any})

(defn parse-xml-response [res]                              ; response format is awful
  (->> (tub/xml->clj res)
       (w/postwalk (fn [x]
                     (cond (map? x)
                           (let [{:keys [tag content]} x]
                             {(u/apply-if (not (nil? tag)) cs/->kebab-case tag)
                              (if (and (vector? content)
                                       (= (count content) 1))
                                (first content)
                                (if (every? map? content)
                                  (if (apply distinct? (map (comp ffirst vec) content))
                                    (apply merge content)
                                    (mapv (comp second first vec) content))
                                  content))})

                           (and (vector? x)
                                (every? string? x)) x
                           :else x)))
       :response
       :data))

(defn on-error [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(def default-opts
  {:error-handler on-error})

(s/defn fetch!
  ([api-route] (fetch! api-route {}))
  ([api-route query-params] (fetch! api-route query-params {}))
  ([api-route :- s/Keyword
    query-params :- QueryParams
    opts]
    (let [url (b/path-for api-routes api-route)
          handler (get opts :handler identity)]
      (println "FETCH " url)
      (GET url (merge default-opts
                      opts
                      {:params (m/map-keys cs/->snake_case query-params)}
                      {:handler (comp handler (if-not (:response-format opts)
                                                parse-xml-response
                                                identity))})))))