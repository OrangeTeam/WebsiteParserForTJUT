package org.orange.parser.util;

public class Objects {
    private Objects() {}

    public boolean equals(Object A, Object B) {
        return A != null ? A.equals(B) : B == null;
    }
}
