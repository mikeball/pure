(ns pure.validations
    (:require [clojure.string :only [blank?] :as str]))

(defn get-message [type rule-name param-name messages raw]
  (let [kw (keyword (name type) (name rule-name))
        msg (messages kw)]
    (if (str/blank? msg) 
      (throw (Exception. (str "message " type "/" (name rule-name) " not found!")))
      (let [nice-name (str/replace (name param-name) #"-" " ")]
        
        (-> msg
            (str/replace #":name" nice-name)
            (str/replace #":raw" raw))
        ))))


        (-> ":name required! :raw"
            (str/replace #":name" "first name")
            (str/replace #":raw" ""))


(defmulti check 
  "Returns error message string if a valid, nil if no errors."
  (fn [v] [(v :type) (first (v :rule))]))

(defmethod check [:string :required] [{:keys [type rule param messages]}]
  (cond (false? (second rule)) nil
        (true? (second rule)) (if (str/blank? (second param)) 
                                (get-message type (first rule) (first param) messages (second param)))
        :default (throw (Exception. "invalid string/required setting, only true or false is allowed."))))

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
      (-> (get-message type message-key (first param) messages (second param))
          (str/replace #":min" (str min))
          (str/replace #":max" (str max)))
      nil)))

(defmethod check [:string :custom] [v]
   (let [custom-fn (-> v :rule second)]
     (custom-fn (-> v :param second))))

(defmethod check [:int :required] [{:keys [type rule param messages]}]
  (cond (false? (second rule)) nil
        (true? (second rule)) (if (not (integer? (second param))) 
                                (get-message type (first rule) (first param) messages (second param)))
        :default (throw (Exception. "invalid int/required setting, only true or false is allowed."))))
