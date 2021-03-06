(ns taoclj.pure.compilation-tests
  (:require [taoclj.pure.compilation :as c]
            [clojure.test :refer [deftest is are]]))


(deftest rules-must-have-at-least-type-and-message
  (is (thrown? Exception (c/compile-rule [:id] [])))
  (is (thrown? Exception (c/compile-rule [:id] [:string]))))


(deftest unknown-type-throws-read-error
  (is (thrown? Exception (c/compile-rule [:id] [:unknown ""]))))

(deftest error-message-is-required-to-be-a-string-or-map
  (is (thrown? Exception (c/compile-rule [:id] [:string :xx]))))

(deftest messages-as-map-requires-default-key
  (is (thrown? Exception (c/compile-rule [:id] [:string {}]))))


; (deftest invalid-string-conditions-are-not-allowed
;  (is (thrown? Exception (c/compile-rule :id [:string :unknown "e*"]))))


;; (deftest invalid-string-length-conditions-are-not-allowed
;;   (is (thrown? Exception (c/compile-rule :id [:string [:length] {}]))))

;; (deftest datetime-conditions-require-a-formatter
;;   (is (thrown? Exception (c/compile-rule :id [:datetime "e*"]))))



; (require '[clojure.test :refer [run-tests]])

; (run-tests 'taoclj.pure.compilation-tests)




;; (deftest simple-2-element-rules-are-read
;;   (are [msg]
;;        (= {:type :string :messages {:default "m"}}
;;                (c/compile-rule :id [:string msg]))
;;        "m"
;;        {:default "m"} ))


;; (deftest conditions-are-read

;;   (is (= {:type :string :required true :messages {:default "m*"}}
;;          (c/read-rule :id [:string :required "m*"])))

;;   (is (= {:type :string :required true :conditions '([:length 5 20]) :messages {:default "m*"}}
;;          (c/read-rule :id [:string :required [:length 5 20] "m*"])))

;;   )














