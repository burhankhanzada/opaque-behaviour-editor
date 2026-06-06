package umlopaquebehaviourbodyeditor;

public class TextRange {
    public final int offset;
    public final int length;
    public final String message;
    
    public TextRange(int offset, int length, String message) {
        this.offset = offset;
        this.length = length;
        this.message = message;
    }
}
