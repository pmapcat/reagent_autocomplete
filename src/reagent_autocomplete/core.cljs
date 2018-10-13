(ns reagent-autocomplete.core
  (:require [reagent.core :as reagent]
            [clojure.string :as cljs-string]))

(defn e->content
  [e]
  (str
   (aget e "target" "value" )))

(defn- change-cur-input
  [app-state input]
  (swap! app-state
         (fn [db ]
           (-> db
               (assoc :cur-input input)
               (assoc :cur-index 0)))))

(defn default-render-fn
  [cur-input selected? text]
  [:div.unstyled-link.complete {:class  (if  selected?  " complete selected " "")
                                :style {:padding "10px"}}
   [:span {:style {:color "green" }}
    cur-input]
   (apply str (drop (count cur-input) text))])

(defn default-compare-fn
  [cur-input item]
  (cljs-string/starts-with?   item  cur-input))

(defn autocomplete-widget
  "Params are:
    {:completes
     [\"hello\"
      \"various\"
      \"different\"
      \"world\"] 
     :can-enter-new? false
     :display-size 10
     :placeholder \"hello world\"
     :compare-fn (fn [cur-input item] (cljs-string/starts-with?   item  cur-input))
     :render-fn (fn [now-input-str selected?-bool item-name-str])
     :submit-fn (fn [data])}"
  [completions params]
  (let [app-state      (or (:app-state params) (reagent/atom {:cur-index 0 :cur-input ""}))
        can-enter-new? (or (:can-enter-new? params) false)
        compare-fn     (or (:compare-fn params) default-compare-fn)
        display-size   (or (:display-size params) 10)
        render-fn      (or (:render-fn params) default-render-fn)
        validate-fn    (or (:validate-fn params) (fn [data] true))
        placeholder    (or (:placeholder params) "Input tags here...")
        submit-fn-raw  (or (:submit-fn params) identity)
        submit-fn
        (fn [input]
          (if-not (empty? input)
            (do
              (submit-fn-raw input)
              (swap! app-state assoc :cur-input ""))
            identity))]
    (fn [completions params]
      (let [aps @app-state
            cur-input (:cur-input aps)
            cur-index (:cur-index aps)
            filtered-items
            (if-not (empty? cur-input)
              (for [[index item] (map list (range) (take display-size (filter (partial compare-fn cur-input)  completions)))]
                {:selected? (= index cur-index)
                 :name   item})
              [])
            complete-placeholder (or (:name (first (filter :selected? filtered-items))) "")
            has-auto-complete? (not (empty? complete-placeholder))]
        [:div {:style {:position "relative"}}
         [:input {:value cur-input
                  :placeholder placeholder
                  :auto-focus true
                  :style {:width "100%" :height "1.9em" :padding-left "10px" :background "transparent"}
                  :type "text"
                  :on-change
                  #(if (validate-fn (e->content %))
                     (change-cur-input app-state (e->content %))
                     identity)
                  :on-key-down
                  #(condp = (aget % "key")
                     "Enter"
                     (cond
                       (and can-enter-new? has-auto-complete?)
                       (submit-fn complete-placeholder)
                       (and (not can-enter-new?) (not has-auto-complete?))
                       identity
                       (and can-enter-new? (not has-auto-complete?))
                       (submit-fn (e->content %))
                       (and (not can-enter-new?)  has-auto-complete?)
                       (submit-fn complete-placeholder))
                     "ArrowDown"
                     (swap! app-state assoc :cur-index (mod (inc cur-index) (count filtered-items)))
                     "ArrowUp"
                     (swap! app-state assoc :cur-index (mod (dec cur-index) (count filtered-items)))
                     "ArrowRight"
                     (change-cur-input app-state complete-placeholder)
                     "Tab"
                     (do
                       (cond
                         (= 1 (count filtered-items))
                         (change-cur-input app-state complete-placeholder)
                         :else
                         (swap! app-state assoc :cur-index (mod (inc cur-index) (count filtered-items))))
                       (.call (aget % "preventDefault") %))
                     identity)}]
         [:input {:style {:width "100%" :height "1.9em" :padding-left "10px" :color "gray" :position "absolute" :top "0" :right "0" :left "0" :bottom "0" :z-index "-1"
                          :background "white"} :disabled true
                  :placeholder complete-placeholder}]
         (if (not (empty? filtered-items))
           [:div {:style {:position "absolute" :right "0" :left "0"   :top "2.3em" :box-shadow "grey 1px 2px 1px 0px" :background "white" :overflow "hidden"  :border "none" :z-index "999"}}
            (for [item filtered-items]
              ^{:key (:name item)}
              [:div {:on-click #(submit-fn (:name item))}
               [render-fn cur-input (:selected? item) (:name item)]])]
           [:div])]))))
