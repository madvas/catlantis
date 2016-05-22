(ns rnntest.handlers
  (:require
    [re-frame.core :refer [register-handler after]]
    [schema.core :as s :include-macros true]
    [print.foo :as pf :include-macros true]
    [rnntest.db :refer [app-db schema]]
    [rnntest.schemas :as sch]
    [rnntest.ios.components.navigation :as nav]
    [com.rpl.specter :as sp]
    [re-frame.middleware :as mid]
    [rnntest.config :as cfg]
    [rnntest.utils :as u]
    [clojure.string :as str]
    [rnntest.api :as api]
    [re-frame.core :as rf]))

;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (when-let [problems (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

(def validate-schema-mw
  (if goog.DEBUG
    (after (partial check-and-throw schema))
    []))

(def basic-mw (comp #_mid/debug mid/trim-v validate-schema-mw))

;; -- Handlers --------------------------------------------------------------

(register-handler
  :initialize-db
  basic-mw
  (fn [_]
    app-db))

(register-handler
  :set-greeting
  basic-mw
  (fn [db [value]]
    (assoc db :greeting value)))


(register-handler
  :nav/push
  basic-mw
  (s/fn [db [screen-name config]]
    (nav/push-screen! screen-name config)
    db))

(register-handler
  :nav/pop
  basic-mw
  (s/fn [db]
    (nav/pop-screen!)
    db))


(register-handler
  :nav/toggle-drawer
  basic-mw
  (s/fn [db [config]]
    (nav/toggle-drawer! config)
    db))

(register-handler
  :categories-res
  basic-mw
  (s/fn [db [res]]
    (assoc db :categories (:categories res))))

(register-handler
  :category-select
  basic-mw
  (s/fn [db [category]]
    (let [category (if (= (:id category) 0) nil category)
          title (-> (str cfg/app-name (u/apply-if string? #(str " (" (str/capitalize %) ")")
                                                  (:name category))))]
      (nav/set-title! title)
      (assoc db :category-selected category))))

(register-handler
  :images-res
  basic-mw
  (s/fn [db [{:keys [images]} category replace?]]
    (let [f (if replace? (constantly images) #(concat % images))]
      (-> db
          (update-in [:images-query :images] f)
          (assoc-in [:images-query :category] category)
          (assoc-in [:images-query :loading?] false)))))

(register-handler
  :images-loading
  basic-mw
  (s/fn [db [loading?]]
    (assoc-in db [:images-query :loading?] loading?)))

(register-handler
  :images-load
  basic-mw
  (s/fn [db [req-category replace?]]
    (let [query-params (cond-> (merge cfg/default-catapi-params
                                      {:results-per-page (get-in db [:images-query :per-page])})
                               (not (nil? req-category)) (assoc :category (:name req-category)))]
      (api/fetch! :images query-params {:handler #(rf/dispatch [:images-res % req-category replace?])})
      (assoc-in db [:images-query :loading?] true))))

(register-handler
  :image-selected
  basic-mw
  (s/fn [db [image]]
    (api/fetch! :facts {:number 1} {:handler         #(rf/dispatch [:facts-res %])
                                    :response-format :json
                                    :keywords?       true})
    (nav/push-screen! :detail)
    (assoc db :image-selected image)))

(register-handler
  :facts-res
  basic-mw
  (s/fn [db [{:keys [facts]}]]
    (assoc db :random-fact (first facts))))

(register-handler
  :image-favoite
  basic-mw
  (s/fn [db [image-id]]
    (api/fetch! :favorite (merge cfg/default-catapi-params
                                 {:sub-id   (get-in db [:user :username])
                                  :image-id image-id})
                {:handler #(println %)})
    (rf/dispatch [:nav/pop])
    db))