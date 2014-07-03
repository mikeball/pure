(ns taoclj.pure
  (:require [clojure.string :only [blank? trim] :as str]
            [taoclj.pure.parsing :as parsing]
            [taoclj.pure.rules :as rules]))


(defn parse
  "Direct parsing of data"
  [type raw] (parsing/parse type raw))


(defn compile-model
  "transforms one map into another with the validation function for each key in the new map"
  [m]
  (let [model-keys (keys m)]
    (zipmap model-keys
            (map (fn [k] (rules/rule-fn k (k m)))
                 model-keys))))

;; (compile-model {:id [:string :required "e*"]})


(defmacro defm
  "A simple way to define a pure model"
  [name & model]
  `(def ~name (compile-model ~@model)))



(defn check
  "Validates a map against a pure model."
  ([model params] (check model params :default))
  ([model params culture]

   (let [model-keys (keys model)

         checks (zipmap model-keys
                        (map (fn [k] ((k model) (k params) culture)  )
                             model-keys))

         errors (into {} (remove nil?
                               (for [k model-keys]
                                 (if-not (-> checks k :ok)
                                   [k (-> checks k :msg)]))))



         ; we need to unit test this, I think there's a problem with model-keys!
         values (into {} (for [k model-keys]
                         [k (-> checks k :val)]))


         error? (not (empty? errors))]

     (-> {:raw params} ; rename params to :raw ?

         ((fn [result] (if-not error? result
                        (assoc result :errors errors))))

         ((fn [result] (if error? result
                        (assoc result :values values))))) )))

