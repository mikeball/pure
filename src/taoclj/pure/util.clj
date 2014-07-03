(ns taoclj.pure.util)


(defn in?
  "Does an element exists in a sequence? Returns nil if nothing found.

  (in? [1 2] 1)
  => true

  (in? [1 2] 3)
  => false

  "
  [sequence element] ;; potentially change the order of these

  (let [s (some #(= element %) sequence)]
    (if (true? s) true false)))
