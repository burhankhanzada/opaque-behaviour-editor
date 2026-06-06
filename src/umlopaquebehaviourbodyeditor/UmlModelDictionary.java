package umlopaquebehaviourbodyeditor;

import java.util.Map;
import java.util.TreeSet;
import org.eclipse.emf.ecore.EObject;

public class UmlModelDictionary {
    public final TreeSet<String> autocompleteWords = new TreeSet<>();
    public final Map<String, Map<String, EObject>> classElements = new java.util.HashMap<>();
    public final Map<String, EObject> globalElements = new java.util.HashMap<>();
    public final Map<String, Map<String, String>> typeMembers = new java.util.HashMap<>();
}
