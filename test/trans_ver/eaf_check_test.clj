(ns trans-ver.eaf-check-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [trans-ver.eaf-check :refer :all]))

(deftest github-issue-1-test
  (testing "test for issue #1 on github"
    (let [file (io/as-file "./github_issue_1.eaf")
          eaf (parse file)
          ort (ort eaf)
          fon (fon eaf)
          errors (summarize-alignment-errors ort fon (second (times eaf)))]
      ;; (print errors)
      (is (not (matches? #"Segment" errors))))))

(deftest github-issue-2-test
  (testing "test for issue #2 on github"
    (let [file (io/as-file "./github_issue_2.eaf")
          eaf (parse file)
          ort (ort eaf)
          fon (fon eaf)
          errors (summarize-alignment-errors ort fon (second (times eaf)))]
      ;; (print errors)
      (is (not (matches? #"Segment" errors))))))

(deftest github-issue-3-test
  (testing "test for issue #3 on github"
    (let [file (io/as-file "./github_issue_3.eaf")
          eaf (parse file)
          ort (ort eaf)
          fon (fon eaf)
          errors (summarize-alignment-errors ort fon (second (times eaf)))]
      ;; (print errors)
      (is (not (matches? #"SLOVOBEZFONREALIZACE" errors))))))

(deftest github-issue-4-test
  (testing "test for issue #4 on github"
    (let [file (io/as-file "./github_issue_4.eaf")
          eaf (parse file)
          ort (ort eaf)
          fon (fon eaf)
          errors (summarize-alignment-errors ort fon (second (times eaf)))]
      ;; (print errors)
      (is (not (matches? #"SLOVOBEZFONREALIZACE" errors))))))
