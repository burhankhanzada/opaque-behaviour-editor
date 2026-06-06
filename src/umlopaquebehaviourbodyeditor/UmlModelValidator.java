package umlopaquebehaviourbodyeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import umlopaquebehaviourbodyeditor.LanguageMapping.LanguageDef;

public class UmlModelValidator {

    public static final String[] COMMON_METHODS = {
        "add", "remove", "clear", "size", "empty", "front", "back", "insert", "erase", 
        "push_back", "pop_back", "begin", "end", "find", "count", "length", "substr", "at"
    };

    public static final String[] MDE4CPP_COLLECTION_METHODS = {
        "add", "insert", "remove", "erase", "clear", "size", "empty", 
        "front", "back", "begin", "end", "at"
    };

    private final UmlModelDictionary dictionary;

    public UmlModelValidator(UmlModelDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public List<TextRange> validateUMLMemberAccess(String text, LanguageDef currentLangDef) {
        List<TextRange> errors = new ArrayList<>();
        if (currentLangDef == null || !currentLangDef.name.equals("C++")) {
            return errors;
        }
        
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("->[ \\t]*([A-Za-z0-9_]+)");
        java.util.regex.Matcher m = p.matcher(text);
        
        while (m.find()) {
            String methodName = m.group(1);
            int methodOffset = m.start(1);
            int methodLength = methodName.length();
            
            String textBefore = text.substring(0, m.start() + 2);
            String rawType = resolveContextTypeFromText(textBefore);
            
            if (rawType != null) {
                boolean isCollection = rawType.startsWith("Bag<") || rawType.startsWith("Set<") || 
                                       rawType.startsWith("OrderedSet<") || rawType.startsWith("Sequence<") ||
                                       rawType.startsWith("Union<") || rawType.startsWith("SubsetUnion<");
                                       
                if (rawType.startsWith("std::shared_ptr<")) {
                    rawType = rawType.substring(16, rawType.length() - 1);
                }
                
                boolean isValid = false;
                
                if (isCollection) {
                    for (String cm : MDE4CPP_COLLECTION_METHODS) {
                        if (cm.equals(methodName)) { isValid = true; break; }
                    }
                } else if (dictionary.classElements.containsKey(rawType) || dictionary.typeMembers.containsKey(rawType)) {
                    Map<String, String> members = dictionary.typeMembers.get(rawType);
                    if (members != null && members.containsKey(methodName)) {
                        isValid = true;
                    }
                    if (!isValid) {
                        for (String cm : COMMON_METHODS) {
                            if (cm.equals(methodName)) { isValid = true; break; }
                        }
                    }
                } else {
                    isValid = true;
                }
                
                if (!isValid) {
                    errors.add(new TextRange(methodOffset, methodLength, "Method '" + methodName + "' is not defined in UML class '" + rawType + "'"));
                }
            }
        }
        return errors;
    }

    private String resolveContextTypeFromText(String textBefore) {
        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("std::(?:weak|shared|unique)_ptr<\\s*([A-Za-z0-9_:<>,\\s]+)\\s*>\\s+([A-Za-z0-9_]+)\\b");
        java.util.regex.Matcher m1 = p1.matcher(textBefore);
        while (m1.find()) {
            String type = m1.group(1);
            String name = m1.group(2);
            if (textBefore.endsWith(name + "->")) {
                return type;
            }
        }
        
        java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("\\b([A-Za-z0-9_:]+)\\s*\\**\\s+([A-Za-z0-9_]+)\\s*(?:=|;)");
        java.util.regex.Matcher m2 = p2.matcher(textBefore);
        while (m2.find()) {
            String type = m2.group(1);
            String name = m2.group(2);
            if (textBefore.endsWith(name + "->") && !type.equals("return") && !type.equals("new") && !type.equals("delete")) {
                return type;
            }
        }
        
        java.util.regex.Pattern p3 = java.util.regex.Pattern.compile("new\\s+([A-Za-z0-9_:]+)\\s*\\(");
        java.util.regex.Matcher m3 = p3.matcher(textBefore);
        while (m3.find()) {
            String type = m3.group(1);
            return type; // Simple fallback
        }
        
        java.util.regex.Pattern p4 = java.util.regex.Pattern.compile("\\b([A-Za-z0-9_:]+)::([A-Za-z0-9_]+)\\(");
        java.util.regex.Matcher m4 = p4.matcher(textBefore);
        while (m4.find()) {
            String type = m4.group(1);
            return type;
        }
        
        int lastParen = textBefore.lastIndexOf('(');
        if (lastParen > 0) {
            java.util.regex.Pattern castPattern = java.util.regex.Pattern.compile("dynamic_pointer_cast<\\s*([A-Za-z0-9_]+)\\s*>");
            java.util.regex.Matcher castMatch = castPattern.matcher(textBefore);
            if (castMatch.find()) {
                return castMatch.group(1);
            }
        }
        
        return null;
    }
}
