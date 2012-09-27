(ns pure.validations
    (:require [clojure.string :only [blank?] :as str]))

(defn get-message [type rule-name param-name messages]
  (let [kw (keyword (name type) (name rule-name))
        msg (messages kw)]
    (if (str/blank? msg) 
      (throw (Exception. (str "message " type "/" (name rule-name) " not found!")))
      (let [nice-name (str/replace (name param-name) #"-" " ")]
        (str/replace msg #":name" nice-name)))))


(defmulti check 
  "Returns error message string if a valid, nil if no errors."
  (fn [v] [(v :type) (first (v :rule))]))

(defmethod check [:string :required] [{:keys [type rule param messages]}]
  (cond (false? (second rule)) nil
        (true? (second rule)) (if (str/blank? (second param)) 
                                (get-message type (first rule) (first param) messages))
        :default (throw (Exception. "invalid string/required setting, only true or false are allowed."))))

(defmethod check [:string :length] [{:keys [type rule param messages]}]
  (let [len (count (second param)) 
        setting (second rule)
        min (first setting) 
        max (second setting)
        short (and (not (nil? min)) (< len min))
        long (and (not (nil? max)) (> len max))
        message-key (cond (and short (contains? messages :string/length-short)) :length-short
                          (and long (contains? messages :string/length-long)) :length-long
                          :default :length)]
    (if (or short long)
      (-> (get-message type message-key (first param) messages)
          (str/replace #":min" (str min))
          (str/replace #":max" (str max)))
      nil)))

(defmethod check [:string :custom] [v]
   (let [custom-fn (-> v :rule second)]
     (custom-fn (-> v :param second))))


(defmethod check [:int :required] [{:keys [type rule param messages]}]
  (cond (false? (second rule)) nil
        (true? (second rule)) (if (not (integer? (second param))) 
                                (get-message type (first rule) (first param) messages))
        :default (throw (Exception. "invalid int/required setting, only true or false is allowed."))))


(defmethod check [:int :range] [{:keys [type rule param messages]}]
  (let [setting (second rule) min (first setting) max (second setting) val (second param)
        under (and (not (nil? min)) (or (nil? val) (< val min)))
        over (and (not (nil? max)) (or (nil? val) (> val max)))
        message-key (cond (and (not (nil? min)) (not (nil? max)) (or (nil? val) under over)) :int/range
                          (and under (contains? messages :int/range-under)) :range-under
                          (and over (contains? messages :int/range-over)) :range-over)]
    (if (or under over)
      (-> (get-message type message-key (first param) messages)
          (str/replace #":min" (str min))
          (str/replace #":max" (str max)))
      nil)))


(defmethod check [:email :required] [{:keys [type rule param messages]}]
  (let [rule-name (first rule) setting (second rule)
        param-name (first param) val (second param)
        not-email-fmt? #(not (re-matches #".+@.+\..+" %))
        required-message (get-message type rule-name  param-name messages)
        invalid-message (get-message type :invalid param-name messages)]

    (cond (false? setting) (cond (str/blank? val) nil
                                 (not-email-fmt? val) invalid-message)
          (true? setting) (cond (str/blank? val) required-message
                                (not-email-fmt? val) invalid-message)
          :default (throw (Exception. "invalid email/required setting, only true or false are allowed.")))))

