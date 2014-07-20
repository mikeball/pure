(ns taoclj.pure.parsers
  (:require [clojure.string :only [trim] :as str]
            [clj-time.format :as tf])
  (:import [org.apache.commons.validator.routines EmailValidator]))




(defn parse-string
  "Verifies raw is a string and trims leading and trailing whitespace"
  [raw _]
  (cond (nil? raw)          {:ok true :val nil}
        (not (string? raw)) {:ok false}
        (str/blank? raw)    {:ok true :val nil}
        :else               {:ok true :val (str/trim raw)}))


(defn parse-int
  "Parses a string into a valid Long."
  [raw _]
  (cond (nil? raw)           {:ok true :val nil}
        (integer? raw)       {:ok true :val raw}
        (not (string? raw))  {:ok false :val nil}
        (str/blank? raw)     {:ok true :val nil}

        :default
        (try                 {:ok true :val (Long. (str/trim raw))}
          (catch Exception e {:ok false :val nil}))))


(defn parse-datetime [raw fmt]
  (cond (nil? raw)           {:ok true :val nil}
        (not (string? raw))  {:ok false :val nil}
        (str/blank? raw)     {:ok true :val nil}

        :else
        (try
          {:ok true :val (tf/parse (tf/formatter fmt) raw)}
          (catch Exception e {:ok false :val nil}))  ))

; (parse-datetime "03/01/2014" "MM/dd/yyyy")


;; (defn parse-email [raw _]
;;   (cond (nil? raw)          {:ok true :val nil}
;;         (not (string? raw)) {:ok false}
;;         (str/blank? raw)    {:ok true :val nil}
;;         :default {:ok (.isValid (EmailValidator/getInstance) raw)
;;                   :val (str/trim raw)}))







