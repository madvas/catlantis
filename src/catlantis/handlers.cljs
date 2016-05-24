(ns catlantis.handlers
  (:require
    [re-frame.core :refer [register-handler after]]
    [schema.core :as s :include-macros true]
    [print.foo :as pf :include-macros true]
    [catlantis.db :refer [app-db schema]]
    [catlantis.shared.navigation :as nav]
    [re-frame.middleware :as mid]
    [catlantis.config :as cfg]
    [clojure.string :as str]
    [catlantis.api :as api]
    [re-frame.core :as rf]))

(.log js/console "here handler")
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (when-let [problems (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

(def validate-schema-mw
  (when goog.DEBUG
    (after (partial check-and-throw schema))))

(def basic-mw [#_mid/debug mid/trim-v validate-schema-mw])

(.log js/console "register init")

(register-handler
  :initialize-db
  basic-mw
  (fn [_]
    app-db))

(.log js/console "after register init")

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
    (update db :categories concat (map #(update % :name str/capitalize) (:categories res)))))

(register-handler
  :category-select
  basic-mw
  (s/fn [db [category]]
    (nav/set-title! (str/capitalize (:name category)))
    (assoc db :category-selected (if (:id category) category nil))))

(register-handler
  :images-res
  basic-mw
  (s/fn [db [images category replace?]]
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
      (api/fetch! :images query-params {:handler
                                        #(rf/dispatch [:images-res (-> % :images)
                                                       req-category replace?])})
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
  :image-favorite
  basic-mw
  (s/fn [db [{:keys [id] :as image} unfavorite?]]
    (api/fetch! :favorite (merge cfg/default-catapi-params
                                 {:sub-id   (get-in db [:user :username])
                                  :image-id (:id image)
                                  :action   (if unfavorite? "remove" "add")}))
    (let [f (if unfavorite?
              (partial remove #(= (:id %) id))
              #(conj % image))]
      (-> db
          (assoc-in [:image-selected :favorite?] (not unfavorite?))
          (update-in [:favorites-query :images] f)))))

(register-handler
  :favorites-load
  basic-mw
  (s/fn [db]
    (let [query-params (cond-> (merge cfg/default-catapi-params
                                      {:sub-id (get-in db [:user :username])}))]
      (api/fetch! :favorites query-params
                  {:handler #(rf/dispatch [:favorites-res (-> % :images)])})
      (assoc-in db [:favorites-query :loading?] true))))

(register-handler
  :favorites-res
  basic-mw
  (s/fn [db [images]]
    (let [images (if (map? images) [(:image images)] images)]
      (-> db
          (assoc-in [:favorites-query :images] images)
          (assoc-in [:favorites-query :loading?] false)))))

(register-handler
  :user-change
  basic-mw
  (s/fn [db [user]]
    (assoc db :user user)))