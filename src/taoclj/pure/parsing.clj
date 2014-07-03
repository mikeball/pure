(ns taoclj.pure.parsing
  (:require [clojure.string :only [trim] :as str])
  (:import [org.apache.commons.validator.routines EmailValidator]))


(defmulti parse "Parses a supplied raw value"
  (fn [type raw] type))

(defmethod parse :default [type _]
  (throw (Exception. (str type " is not a suppored type!"))))


(defmethod parse :string [_ raw]

  (cond (nil? raw)          {:ok true :val nil}
        (not (string? raw)) {:ok false}
        (str/blank? raw)    {:ok true :val nil}
        :else               {:ok true :val (str/trim raw)}))



(defmethod parse :int [_ raw]
  (cond (nil? raw)           {:ok true :val nil}
        (integer? raw)       {:ok true :val raw}
        (not (string? raw))  {:ok false :val nil}
        (str/blank? raw)     {:ok true :val nil}
        :default
        (try                 {:ok true :val (Long. (str/trim raw))}
          (catch Exception e {:ok false :val nil}))))


(defmethod parse :email [_ raw]
  (cond (nil? raw)          {:ok true :val nil}
        (not (string? raw)) {:ok false}
        (str/blank? raw)    {:ok true :val nil}
        :default {:ok (.isValid (EmailValidator/getInstance) raw)
                  :val (str/trim raw)}))

; (parse :email "bob@bob")
; (parse :int " 2 ")
; (parse :string " a ")

