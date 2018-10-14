;; @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
;; @ Copyright (c) Michael Leachim                                                      @
;; @ You can find additional information regarding licensing of this work in LICENSE.md @
;; @ You must not remove this notice, or any other, from this software.                 @
;; @ All rights reserved.                                                               @
;; @@@@@@ At 2018-14-10 14:25 <mklimoff222@gmail.com> @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

(ns org.clojars.thereisnodot.reagent_autocomplete
  (:require [reagent.core :as reagent]
            [clojure.string :as cljs-string]))

(def react-keys
  {:enter      "Enter"
   :arrow-down "ArrowDown"
   :arrow-up    "ArrowUp"
   :arrow-right "ArrowRight"
   :tab "Tab"})

(def default-params
  {:app-state (reagent/atom {:cur-index 0 :cur-input ""})
   
   :can-enter-new? true
   :display-size 10
   :placeholder "Input anything here..."
   :autofocus? true
   
   :dropdown-style
   {:position "absolute" :right "0" :left "0"   :top "2.3em" :box-shadow "grey 1px 2px 1px 0px" :background "white" :overflow "hidden"  :border "none" :z-index "999"}
   :input-style
   {:width "100%" :height "1.9em" :padding-left "10px" :background "transparent"}
   :background-input-style
   {:width "100%" :height "1.9em" :padding-left "10px" :color "gray" :position "absolute" :top "0" :right "0" :left "0" :bottom "0" :z-index "-1" :background "white"}
   
   :compare-fn
   (fn [cur-input item]
     "Checking whether input matches with the item"
     (cljs-string/starts-with?   item  cur-input))
   :validate-fn
   (fn [data] true)
   :submit-fn
   (fn [data]
     "When user clicks enter:"
     (.log js/console "Submitted now: " data))
   :render-fn
   (fn [cur-input selected? predicted-input]
     [:div.unstyled-link.complete {:class  (if  selected?  " complete selected " "")
                                   :style {:padding "10px"}}
      [:span {:style {:color "green" }}
       cur-input]
      (apply str (drop (count cur-input) predicted-input))])})

(defn- e->content
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

(defn autocomplete-widget
  [completions params]
  (let [{:keys [app-state
           can-enter-new? display-size placeholder autofocus?
           dropdown-style input-style background-input-style
                compare-fn validate-fn submit-fn render-fn]}
        (merge default-params params)
        wrap-submit-fn
        (fn [input]
          (if-not (empty? input)
            (do
              (submit-fn input)
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
                  :auto-focus autofocus?
                  :style input-style 
                  :type "text"
                  :on-change
                  #(if (validate-fn (e->content %))
                     (change-cur-input app-state (e->content %))
                     identity)
                  :on-key-down
                  #(condp = (aget % "key")
                     (:enter react-keys)
                     (cond
                       (and can-enter-new? has-auto-complete?)
                       (wrap-submit-fn complete-placeholder)
                       (and (not can-enter-new?) (not has-auto-complete?))
                       identity
                       (and can-enter-new? (not has-auto-complete?))
                       (wrap-submit-fn (e->content %))
                       (and (not can-enter-new?)  has-auto-complete?)
                       (wrap-submit-fn complete-placeholder))
                     (:arrow-down react-keys)
                     (swap! app-state assoc :cur-index (mod (inc cur-index) (count filtered-items)))
                     (:arrow-up react-keys)
                     (swap! app-state assoc :cur-index (mod (dec cur-index) (count filtered-items)))
                     (:arrow-right react-keys)
                     (change-cur-input app-state complete-placeholder)
                     (:tab react-keys)
                     (do
                       (cond
                         (= 1 (count filtered-items))
                         (change-cur-input app-state complete-placeholder)
                         :else
                         (swap! app-state assoc :cur-index (mod (inc cur-index) (count filtered-items))))
                       (.call (aget % "preventDefault") %))
                     identity)}]
         [:input {:style  background-input-style :disabled true
                  :placeholder complete-placeholder}]
         (if (not (empty? filtered-items))
           [:div {:style dropdown-style}
            (for [item filtered-items]
              ^{:key (:name item)}
              [:div {:on-click #(wrap-submit-fn (:name item))}
               [render-fn cur-input (:selected? item) (:name item)]])]
           [:div])]))))
