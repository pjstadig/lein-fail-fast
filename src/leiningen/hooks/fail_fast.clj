(ns leiningen.hooks.fail-fast
  (:use [robert.hooke]
        [leiningen.compile :only [eval-in-project]]
        [clojure.pprint :only [pprint]]))

(defn wrap [form]
  `(do
     (add-hook
      #'clojure.test/do-report
      (fn [f# & args#]
        (let [result# (apply f# args#)]
          (when (contains? #{:fail :error}
                           (:type (first args#)))
            (System/exit 0))
          result#)))
     ~form))

(defn fail-fast-hooke [f project form & [handler skip-auto init & args]]
  (apply f project (wrap form) handler skip-auto
         `(do (require 'clojure.test)
              (require 'robert.hooke)
              ~init)
         args))

(if (System/getenv "LEIN_FAIL_FAST")
  (add-hook #'eval-in-project fail-fast-hooke))
