public enum Floor {
    B4, B3, B2, B1, F1, F2, F3, F4, F5, F6, F7;
    
    public Floor getUpper() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal < values().length) {
            return values()[nextOrdinal];
        }
        throw new RuntimeException("No upper floor");
    }
    
    public Floor getLower() {
        int prevOrdinal = this.ordinal() - 1;
        if (prevOrdinal >= 0) {
            return values()[prevOrdinal];
        }
        throw new RuntimeException("No lower floor");
    }
}