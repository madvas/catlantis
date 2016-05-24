(ns catlantis.shared.navigation                             ; I should probably complete this and make into a library ;)
  (:refer-clojure :exclude [pop!])
  (:require-macros [print.foo :as pf])
  (:require [reagent.core :as r]
            [catlantis.utils :as u]
            [schema.core :as s :include-macros true]
            [camel-snake-kebab.core :as cs :include-macros true]))

(def Navigation (aget (js/require "react-native-navigation") "Navigation"))

(def empty-render-state
  {:nav-stack '()
   :drawers   nil})

(def empty-state {:rendered             empty-render-state
                  :render-queue         empty-render-state
                  :registered-screens   {}
                  :first-screen-render? false
                  :navigator-style      {}})

(def default-drawer-state {:to :closed})

(defonce *nav-state* (atom empty-state))
(def ^:private o s/optional-key)

(s/defschema NavScreenConfig
  {:screen          s/Keyword
   (o :title)       s/Str
   (o :screen-type) (s/enum :screen :modal :light-box)
   s/Any            s/Any})

(s/defschema NavScreenConfigNoName (dissoc NavScreenConfig :screen))

(s/defschema Drawer {(o :to)       (s/enum :open :closed)
                     (o :side)     (s/enum :left :right)
                     (o :animated) s/Bool
                     s/Any         s/Any})

(s/defschema Drawers (s/maybe {(s/enum :left :right) Drawer}))

(s/defschema NavScreen
  {:component                 s/Any
   :config                    NavScreenConfig
   (o :on-navigator-event-fn) s/Any})

(s/defschema Navigator (s/pred #(aget % "navigatorID")))

(s/defschema NavStackItemInQueue
  {:config NavScreenConfig})

(s/defschema NavStackItem
  (assoc NavStackItemInQueue :navigator Navigator))

(s/defschema NavStack [NavStackItem])

(s/defschema RenderedState
  {:nav-stack NavStack
   :drawers   Drawers})

(s/defschema RenderQueue
  {:nav-stack [NavStackItemInQueue]
   :drawers   Drawers})

(s/defschema NavState
  {:rendered             RenderedState
   :render-queue         RenderQueue
   :registered-screens   {s/Keyword NavScreenConfig}
   :first-screen-render? s/Bool
   :navigator-style      {s/Any s/Any}})

(s/defschema NavStateAtom (s/atom NavState))

(s/defschema NavInitConfig
  {(o :persist-state?) s/Bool
   (o :screen)         (s/cond-pre s/Keyword NavScreenConfig)
   (o :drawer)         {(o :left) (s/pred map?)
                        (o :right) (s/pred map?)
                        s/Any s/Any}
   s/Any               s/Any})


(defn get-nav-state []
  @*nav-state*)

(declare action!)

(defn set-on-navigator-event! [navigator config callback]
  (.setOnNavigatorEvent navigator (comp #(callback % config navigator) u/js->cljk)))

(defn register-component! [name component]
  (.registerComponent Navigation name (fn [] component)))

(s/defn push-nav-stack! [item :- NavStackItem]
  (swap! *nav-state* update-in [:rendered :nav-stack] conj item))

(s/defn pop-nav-stack! []
  (swap! *nav-state* update-in [:rendered :nav-stack] pop))

(s/defn update-nav-stack-top! [nav-state-atom f]
  (swap! nav-state-atom update-in [:rendered :nav-stack]
         #(conj (rest %) (f (peek %)))))

(s/defn assoc-nav-stack-top! [nav-state-atom k v]
  (update-nav-stack-top! nav-state-atom #(assoc % k v)))

(s/defn assoc-nav-stack-top-cfg! [nav-state-atom k v]
  (update-nav-stack-top! nav-state-atom #(assoc-in % [:config k] v)))

(defn registered-cfg [screen-name]
  (get-in @*nav-state* [:registered-screens screen-name]))

(s/defn merge-with-default-cfg
  ([config :- NavScreenConfig]
    (merge-with-default-cfg (:screen config) config))
  ([screen-name :- s/Keyword
    config :- NavScreenConfigNoName]
    (let [nav-style (apply merge
                           (map :navigator-style
                                [@*nav-state* (registered-cfg screen-name) config]))]
      (merge (registered-cfg screen-name)
             config
             {:navigator-style nav-style}))))


(declare render-queued-drawers!)
(declare pop-render-queue!)
(declare push-screen!)

(s/defn has-rendered? [nav-state :- NavState]
  (seq (-> nav-state :rendered :nav-stack)))

(s/defn has-render-queue? [nav-state :- NavState]
  (seq (-> nav-state :render-queue :nav-stack)))

(s/defn get-top-navigator :- Navigator
  [rendered-state :- RenderedState]
  (-> rendered-state :nav-stack first :navigator))

(s/defn get-top-screen-cfg :- (s/maybe NavScreenConfig)
  [render-state]
  (-> render-state :nav-stack first :config))

(s/defn ^:private wrap-screen-component
  [{:keys [config on-navigator-event-fn component]} :- NavScreen]
  (r/create-class
    {:component-will-mount
     (fn [this]
       (let [{:keys [first-screen-render?]} @*nav-state*
             props (:props (u/obj->hash-map this))
             navigator (:navigator props)
             screen-cfg (if (has-render-queue? @*nav-state*)
                          (pop-render-queue! *nav-state*)
                          (merge config (-> (:config props)
                                            u/walk-kebabize-keys
                                            (u/keywordize-map-vals-at :screen :screen-type))))]
         (this-as c
           (r/set-state c {:config screen-cfg}))
         (push-nav-stack!
           {:config    screen-cfg
            :navigator navigator})
         (set-on-navigator-event! navigator screen-cfg on-navigator-event-fn)
         (when first-screen-render?
           (action! :dismiss-light-box)
           (render-queued-drawers! *nav-state*)
           (swap! *nav-state* assoc :first-screen-render? false))
         (when-let [cfg (get-top-screen-cfg (:render-queue @*nav-state*))]
           (push-screen! (:screen cfg) cfg true))))
     :reagent-render
     (fn [props]
       (this-as c
         [component (merge props (r/state c))]))}))


(defn register-reagent-component!
  ([screen-name component] (register-reagent-component! screen-name component nil))
  ([screen-name component config]
   (let [component (r/reactify-component component)]
     (when config
       (doseq [field (u/walk-camelize-keys config)]
         (aset component (name (key field)) (clj->js (val field)))))
     (register-component! (name screen-name) component))))

(s/defn register-screen! [screen :- NavScreen]
  (let [config (:config screen)
        screen-name (:screen config)
        component (wrap-screen-component screen)]
    (register-reagent-component! screen-name component config)
    (swap! *nav-state* assoc-in [:registered-screens screen-name] config)))

(s/defn action!
  ([action-name]
    (action! action-name nil {}))
  ([action-name screen-name]
    (action! action-name screen-name {}))
  ([action-name :- s/Keyword
    screen-name :- (s/maybe s/Keyword)
    config :- (s/maybe (s/cond-pre NavScreenConfigNoName Drawer))]
    (let [config (merge {:screen screen-name} config)
          navigator (get-top-navigator (:rendered @*nav-state*))
          f! (aget navigator (cs/->camelCaseString action-name))]
      (.apply f! navigator (clj->js [(u/walk-camelize-keys (u/ensure-map config))])))))

(s/defn nav-stack-empty? [nav-state :- NavState]
  (not (seq (:nav-stack nav-state))))

(s/defn drawer-empty? [side :- s/Keyword nav-state :- NavState]
  (not (get (:drawers nav-state) side)))

(defn push! [screen-name config]
  (action! :push screen-name config))

(defn pop! [screen-name config]
  (action! :pop screen-name config))

(defn reset-to! [screen-name config]
  (action! :reset-to screen-name config))

(defn show-modal! [screen-name config]
  (action! :show-modal screen-name config))

(defn dismiss-modal! [screen-name config]
  (action! :dismiss-modal screen-name config))

(defn dismiss-all-modals! [screen-name config]
  (action! :dismiss-all-modals screen-name config))

(defn show-light-box! [screen-name config]
  (action! :show-light-box screen-name config))

(defn dismiss-light-box! [screen-name config]
  (action! :dismiss-light-box screen-name config))

(defn handle-deep-link! [screen-name config]
  (action! :handle-deep-link screen-name config))

(defn set-buttons! [screen-name config]
  (action! :set-buttons screen-name config))

(defn set-title! [title]
  (action! :set-title nil {:title title})
  (assoc-nav-stack-top-cfg! *nav-state* :title title))

(s/defn toggle-drawer!
  ([] (toggle-drawer! {}))
  ([config :- (s/maybe Drawer)]
    (let [drawers (:drawers (:rendered @*nav-state*))
          side (get config :side (ffirst drawers))
          to (get config :to (u/opposite (:to (side drawers)) :open :closed))
          config (assoc (u/ensure-map config) :to to :side side)]
      (action! :toggle-drawer nil config)
      (swap! *nav-state* assoc-in [:rendered :drawers (:side config)] config))))

(defn toggle-tabs! [screen-name config]
  (action! :toggle-tabs screen-name config))

(defn set-tab-badge! [screen-name config]
  (action! :set-tab-badge screen-name config))

(defn switch-to-tab! [screen-name config]
  (action! :switch-to-tab screen-name config))

(defn toggle-nav-bar! [screen-name config]
  (action! :toggle-nav-bar screen-name config))

(defn init-drawer
  [side drawer]
  (when (side drawer)
    {:drawers {side (merge default-drawer-state (side drawer))}}))


(s/defn init-render-queue :- RenderQueue
  [{:keys [screen drawer]} :- NavInitConfig]
  (let [screen (if (keyword? screen) {:screen screen} screen)]
    (merge-with merge
                {:nav-stack (list {:config (merge-with-default-cfg screen)})}
                (init-drawer :left drawer)
                (init-drawer :right drawer))))

(s/defn clear-navigators [render-state :- RenderedState]
  (update render-state :nav-stack
          (fn [nav-stack]
            (map #(dissoc % :navigator) nav-stack))))

(defn screen-type->action [type add?]
  (let [f (if add? first second)]
    (f (case type
         :modal [:show-modal :dismiss-modal]
         :light-box [:show-light-box :dismiss-light-box]
         [:push :pop]))))

(s/defn with-no-anim [config :- NavScreenConfig]
  (case (:screen-type config)
    :modal (assoc config :animation-type :none)
    (assoc config :animated false)))

(s/defn screen-name->action-name :- s/Keyword
  ([config :- NavScreenConfig add?]
    (screen-type->action (:screen-type config) add?)))

(s/defn push-screen!
  ([screen-name] (push-screen! screen-name {}))
  ([screen-name config] (push-screen! screen-name config false))
  ([screen-name :- s/Keyword
    config :- (s/maybe NavScreenConfigNoName)
    no-anim? :- s/Bool]
    (let [config (merge-with-default-cfg screen-name (u/ensure-map config))
          action-name (screen-name->action-name config true)]
      (u/timeout-if
        (and (= action-name :show-light-box)                ; Lightbox won't show when we dismiss and show
             (has-render-queue? @*nav-state*))              ; instantly
        #(action!
          action-name
          (:screen config)
          (-> (u/apply-if no-anim? with-no-anim config)
              (assoc :pass-props {:config config}))) 500))))

(s/defn pop-screen!
  ([] (pop-screen! {} false))
  ([no-anim?] (pop-screen! {} no-anim?))
  ([config :- NavScreenConfigNoName no-anim?]
    (let [config (merge (get-top-screen-cfg (:rendered @*nav-state*))
                        config)]
      (action!
        (screen-name->action-name config false)
        (:screen config)
        (u/apply-if no-anim? with-no-anim config))
      (pop-nav-stack!))))

(s/defn ^:private render-queued-drawers!
  [nav-state-atom :- NavStateAtom]
  (doseq [drawer (get-in @nav-state-atom [:render-queue :drawers])]
    (toggle-drawer!
      (merge (val drawer) {:animated false
                           :side     (key drawer)})))
  (swap! nav-state-atom assoc-in [:render-queue :drawers] (:drawers empty-render-state)))

(s/defn ^:private pop-render-queue!
  [nav-state-atom :- NavStateAtom]
  (let [config (get-top-screen-cfg (:render-queue @nav-state-atom))]
    (swap! nav-state-atom update-in [:render-queue :nav-stack] rest)
    config))

(s/defn rendered->render-queue
  [rendered-state :- RenderedState]
  (-> rendered-state
      clear-navigators
      (update :nav-stack reverse)))

(s/defn start-single-screen-app!
  [{:keys [persist-state? navigator-style] :as init-cfg} :- NavInitConfig]
  (swap! *nav-state* assoc :navigator-style (u/ensure-map navigator-style))

  (let [render-queue (if (and persist-state? (has-rendered? @*nav-state*))
                       (-> @*nav-state* :rendered rendered->render-queue)
                       (init-render-queue init-cfg))
        screen-cfg (-> render-queue :nav-stack first :config)]
    (swap! *nav-state* assoc
           :render-queue render-queue
           :rendered empty-render-state
           :first-screen-render? true)
    (.startSingleScreenApp Navigation
                           (-> init-cfg
                               (assoc :screen (merge-with-default-cfg screen-cfg))
                               u/clj->camel->js))))

(defn start-tab-based-app! [config]                         ; Not yet implemented
  (.startTabBasedApp Navigation (u/clj->camel->js config)))

