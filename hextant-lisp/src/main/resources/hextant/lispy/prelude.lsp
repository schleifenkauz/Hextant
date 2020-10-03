(defun (map f lst) (if (nil? lst) '() (cons (f (car lst)) (map f (cdr lst)))))
