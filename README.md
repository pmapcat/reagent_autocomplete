# Reagent autocomplete widget

[![Clojars Project](https://img.shields.io/clojars/v/thereisnodot/reagent_autocomplete.svg)](https://clojars.org/thereisnodot/reagent_autocomplete)

<img src="https://raw.githubusercontent.com/MichaelLeachim/reagent_autocomplete/master/images/capture.png" style="text-align:center;"></img>

Reagent autocomplete is a  **Reagent** autocomplete widget. 
It supports:

* Cycling through options with Tab, Up and Down arrows
  <img src="https://raw.githubusercontent.com/MichaelLeachim/reagent_autocomplete/master/images/cycling.gif" style="text-align:center;"></img>
* Autocomplete current option with Right arrow or Tab
  <img src="https://raw.githubusercontent.com/MichaelLeachim/reagent_autocomplete/master/images/right_arrow.gif" style="text-align:center;"></img>
* Complete, if there is only a single option
  <img src="https://raw.githubusercontent.com/MichaelLeachim/reagent_autocomplete/master/images/complete_single.gif" style="text-align:center;"></img>
* *Optional* forbid complete if there are no options
* Customizable styling and no framework dependency

## Usage

### Getting started

Add dependency to the project.clj

[![Clojars Project](https://img.shields.io/clojars/v/thereisnodot/reagent_autocomplete.svg)](https://clojars.org/thereisnodot/reagent_autocomplete)

Then. This set up should yield the same result as on the screenshot

```clojure

(ns stuff.app
  (:require [reagent.core :as reagent]
            [thereisnodot.reagent-autocomplete.core :as autocomplete]))
            
(defn some-component []
  [:div {:style {:width "100%"}}
   [autocomplete/autocomplete_widget
   ["albania" "algeria" "andorra" "angola"]
    {:can-enter-new? false
     :display-size 5
     :placeholder "Enter any city"
     :submit-fn
     (fn [item]
       (.log js/console "Submitted: " item))}]])
       
(defn init []
  (reagent/render-component [some-component]
                            (.getElementById js/document "container")))

```

Below we list additional parameters that can be set. 

### Params

#### Common
```clojure
   {:app-state (reagent/atom {:cur-index 0 :cur-input ""})
    :can-enter-new? true ;;whether you can submit data that is not in the list of completions
    :display-size 10
    :placeholder "Input anything here..."
    :autofocus? true}
```



#### Actions (Render/Filter/Submit) functions

##### Complete and submit policy

User can submit input if:

* The input does not pass `:validate-fn`. Nothing gets submitted
* There is no input. Nothing will get submitted
* There is existing completion, that will be submitted. **U**SA, the **USA** will be submitted
* There is no completion, but the user can enter new. Their input will be submitted. **ADLSKJDKAJ** will be submitted. 
* There is no completion and user cannot enter new. Their input won't be submitted. 

##### Render functions 

These govern how to display the data. 

######  Render

Function that renders autocomplete dropdown list. 

```clojure
{:render-fn
  (fn [cur-input selected? text]
    [:div.reagent-autocomplete-item 
      {:style {:padding "0.5em"
               :background (if  selected? "#dcdbfa" "white")
               :font-size "1em" :cursor "pointer"}}
    [:span cur-input]
    [:b (apply str (drop (count cur-input) text))]])}
```

Function that renders search button. 
In the default case, we see that this button rendered 
only when the policy matches. (I.e. search can be performed)

```clojure
 {:render-click-submit-fn
   (fn [can-submit? on-click]
     (if can-submit?
       [:div
        {:on-click on-click
         :style {:color "#dcdbfa" :cursor "pointer"}}
        [:img {:src search-icon}]]))}
```

##### Submit
If FN is validated and matches policy, this function will be called. 
After this call the app state will be reset. 

```clojure
{:submit-fn
  (fn [data]
    (.log js/console "Submitted the following: " data))}
```

##### Validate input
Default is 

```clojure
{:validate-fn
  (fn [data] true)}
```

##### Compare input

Is a filter function that should return `bool`. 
Decides whether to match the input with the output.

```clojure
{:compare-fn
  (fn [cur-input item]
    "Checking whether input matches with the item"
    (cljs-string/starts-with?   item  cur-input))}
```

#### Styling

Defaults are these. 


```clojure 
  {:dropdown-style
   {:position "absolute" :right "0" :left "0" 
    :top "4em" :box-shadow "grey 1px 2px 1px 0px" 
    :background "white" :overflow "hidden"  :border "none" :z-index "999"}
    
   :input-style
   {:width "100%" :height "2em" :font-size "1.5em"
    :padding-left "10px" :background "transparent" :border "none"}
    
   :background-input-style
   {:width "100%" :height "2em" :font-size "1.5em" :padding-left "10px"
    :color "gray" :position "absolute" :top "0" :right "0" :left "0"
    :bottom "0" :z-index "-1" :background "white" :border "none"}
    
   :click-submit-style
   {:position "absolute" :right "0" :top "0.3em" :padding "0.5em"}
   
   :parent-div-style
   {:position "relative" :box-shadow "1px 1px 1px gray"}
   
   :general-style ".reagent-autocomplete-input:focus{outline:none;}
     .reagent-autocomplete-item:hover{background:#dcdbfa !important;}"}
```

## License

MIT
