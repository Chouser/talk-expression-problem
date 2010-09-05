(ns polydraw
  (:import (clojure.lang PersistentQueue)
           (javax.swing JFrame)
           (java.awt Point Graphics Frame) 
           (java.awt.geom AffineTransform)
           (java.awt.event WindowListener)))

(defprotocol Shape
  (draw [shape y g])
  (height [shape]))

(defrecord Rect []
  Shape
  (draw [_ y g]
    (.fillRect g 10 (+ 10 y) 101 50))
  (height [_]
    50))

(extend-type String
  Shape
  (draw [this y g]
    (.drawString g this 10 (+ 10 y)))
  (height [this]
    50))


(def offset (ref 0))

(def shapes (ref PersistentQueue/EMPTY))

(dosync (alter shapes conj (Rect.)))
(dosync (alter shapes conj (Rect.)))
(dosync (alter shapes conj "Hello World!"))

(def margin 10)

(def f
  (proxy [Frame] ["Shape demo"]
    (paint [g]
      (reduce (fn [y shape]
                (draw shape y g)
                (+ margin y (height shape)))
              20
              @shapes))))

(.addWindowListener f
  (reify WindowListener
    (windowClosing [_ _] (.dispose f))
    (windowActivated [_ _])
    (windowClosed [_ _])
    (windowDeactivated [_ _])
    (windowDeiconified [_ _])
    (windowIconified [_ _])
    (windowOpened [_ _])))

(.show f)
