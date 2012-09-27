(ns pure.parsing
    (:require [clojure.string :only [trim] :as str]))


(defmulti parse "Parses a supplied raw value"
  (fn [type raw] type))

(defmethod parse :default [_ _] (throw (Exception. (str type " is not a suppored type!"))))

(defmethod parse :string [_ raw]
  (if raw (str/trim raw)))

(defmethod parse :int [_ raw]
  (cond (integer? raw) raw
        (string? raw) (if-let [match (re-matches #"\d+" raw)] 
                        (read-string match))
        :default nil))


(defmethod parse :email [_ raw]
  (if raw (str/trim raw)))
