# reagent_autocomplete WIP

Reagent autocomplete is a  simple **Reagent** autocomplete widget. 
It supports:

* Cycling through options with Tab, Up and Down arrows
* Autocomplete current option with Right arrow or Tab
* Complete, if there is only a single option
* Optional forbid auto completing if there are no options
* Easy styling and  integration with frameworks


## Usage




The following parameters are available

```clojure

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
```

## License

MIT
