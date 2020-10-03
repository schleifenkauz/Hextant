(defun (map f lst) (if (nil? lst) '() (cons (f (car lst)) (map f (cdr lst)))))
(defmacro (dbg e) `(begin (print ',e) (print ,e)))