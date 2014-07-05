(ns taoclj.pure.parsing
  (:require [clojure.string :only [trim] :as str]
            [clj-time.format :as f])
  (:import [org.apache.commons.validator.routines EmailValidator]))



(defn parse-string [raw]
  (cond (nil? raw)          {:ok true :val nil}
        (not (string? raw)) {:ok false}
        (str/blank? raw)    {:ok true :val nil}
        :else               {:ok true :val (str/trim raw)}))

(defn parse-int [raw]
  (cond (nil? raw)           {:ok true :val nil}
        (integer? raw)       {:ok true :val raw}
        (not (string? raw))  {:ok false :val nil}
        (str/blank? raw)     {:ok true :val nil}
        :default
        (try                 {:ok true :val (Long. (str/trim raw))}
          (catch Exception e {:ok false :val nil}))))

(defn parse-email [raw]
  (cond (nil? raw)          {:ok true :val nil}
        (not (string? raw)) {:ok false}
        (str/blank? raw)    {:ok true :val nil}
        :default {:ok (.isValid (EmailValidator/getInstance) raw)
                  :val (str/trim raw)}))


(defn parse [type raw options]
  (case type
    :string    (parse-string raw)
    :int       (parse-int raw)
    :email     (parse-email raw)
    :datetime  (parse-datetime raw options)
    :default   (throw (Exception. (str type " is not a suppored type!")))
    ))

; (parse :email "bob@bob")
; (parse :int " 2 ")
; (parse :string " a ")


