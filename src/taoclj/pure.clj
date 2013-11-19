(ns taoclj.pure
  (:require [clojure.string :only [blank? trim] :as str]
            [taoclj.pure.validations :as validations]
            [taoclj.pure.parsing :as parsing]))

(defn parse [type raw] (parsing/parse type raw))

(defn- get-ordered-rules [type]
  (cond (= type :string) [:required :length :custom]
        (= type :int) [:required :range]
        (= type :email) [:required :custom]
        :default []))


(defn- check-for-error
  "Check a value against all rules and returns first error found, nil if no errors found"
  [type rules messages param]
  (first (filter #(not (nil? %))
                 (for [rule-name (get-ordered-rules type)]
                   (let [rule (find rules rule-name)]
                     (if-let [rule (find rules rule-name)]
                       (validations/check {:type type 
                                           :rule rule 
                                           :param param 
                                           :messages messages})))))))


(defn check-rules [{:keys [type param rules messages]}]
  (let [val (parsing/parse type (second param))]
    [val (check-for-error type rules messages [(first param) val])]))


(defn check-all [model messages params]
  (for [name (keys model)]
    [name (check-rules {:type (-> model name :type)
                        :rules (-> model name)
                        :messages messages
                        :param [name (params name)]})]))


(defn validate
  "Validates a supplied map of parameters against a model. If errors are found, they are placed in the errors in returned map. Only parameter names in the allowed list will be parsed, validated and placed into the values map."
  [params model messages]
  (let [results (check-all model messages params)
        errors (reduce (fn [errors [name [_ error]]] 
                         (if error (assoc errors name error) errors)) {} results)
        values (reduce (fn [values [name [val _]]] (assoc values name val)) {} results)]
    {:errors (if-not (empty? errors) errors)
     :params params
     :values values}))

