(ns dice-and-clocks.clocks)


(def clocks [{:key :four-b :faces ["4b0.png" "4b1.png" "4b2.png" "4b3.png" "4b4.png"]}
             {:key :four-o :faces ["4o0.png" "4o1.png" "4o2.png" "4o3.png" "4o4.png"]}
             {:key :six-b :faces ["6b0.png" "6b1.png" "6b2.png" "6b3.png" "6b4.png" "6b5.png" "6b6.png"]}
             {:key :six-o :faces ["6o0.png" "6o1.png" "6o2.png" "6o3.png" "6o4.png" "6o5.png" "6o6.png"]}
             {:key :eight-b :faces ["8b0.png" "8b1.png" "8b2.png" "8b3.png" "8b4.png" "8b5.png" "8b6.png" "8b7.png" "8b8.png"]}
             {:key :eight-o :faces ["8o0.png" "8o1.png" "8o2.png" "8o3.png" "8o4.png" "8o5.png" "8o6.png" "8o7.png" "8o8.png"]}
             {:key :twelve-b :faces ["12b00.png" "12b01.png" "12b02.png" "12b03.png" "12b04.png" "12b05.png" "12b06.png" "12b07.png" "12b08.png" "12b09.png" "12b10.png" "12b11.png" "12b12.png"]}
             {:key :twelve-o :faces ["12o00.png" "12o01.png" "12o02.png" "12o03.png" "12o04.png" "12o05.png" "12o06.png" "12o07.png" "12o08.png" "12o09.png" "12o10.png" "12o11.png" "12o12.png"]}
             {:key :aw-b :faces ["AWb00.png" "AWb03.png" "AWb06.png" "AWb09.png" "AWb10.png" "AWb11.png" "AWb12.png"]}
             {:key :aw-o :faces ["AWo00.png" "AWo03.png" "AWo06.png" "AWo09.png" "AWo10.png" "AWo11.png" "AWo12.png"]}
])

(def clock-types (map (fn [{:keys [key faces]} _] {:key key :face (second faces)}) clocks))

(defn get-face [key tic]
  (let [key (keyword key)]
  (get (:faces (first (filter #(= key (:key %)) clocks))) tic)
))
; (keyword :test)
; (get-face :four-b 1)
; (keys clocks)
; (map {keys})
; (reduce-kv (fn [m k v] (assoc m k (second v))) {} clocks)
; (reduce-kv (fn [m k v] (assoc m k {:face (second (:faces v)) :type (:type v)})) {} clocks)
; clock-types