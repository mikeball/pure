(ns pure.core
  (:require [clojure.string :only [blank? trim] :as str]
            [pure.validations :as validations]
            [pure.parsing :as parsing]))


(defn errors? [validation]
  (if (nil? (keys (validation :errors))) false true))


(defn- get-ordered-rules [type]
  (cond (= type :string) [:required :length :custom]
        (= type :int) [:required :range]
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


(defn check-all [allowed model messages params]
  (for [name allowed]
    [name (check-rules {:type (-> model name :type)
                        :rules (-> model name)
                        :messages messages
                        :param [name (params name)]})]))


(defn validate [params model allowed messages]
  (let [results (check-all allowed model messages params)] 
    {:errors (reduce (fn [errors [name [_ error]]] 
                       (if error (assoc errors name error) errors))
                     {} results)
     :values (reduce (fn [values [name [val _]]] (assoc values name val))
                     {} results)
     :params params}))

