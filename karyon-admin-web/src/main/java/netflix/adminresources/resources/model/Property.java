package netflix.adminresources.resources.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "property")
public class Property {
    private String name;
    private String value;
    private List<PropertySource> sources;

    public Property(String name, String value, List<PropertySource> sources) {
        this.name = name;
        this.value = value;
        this.sources = sources;
    }

    public Property() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<PropertySource> getSources() {
        return sources;
    }

    public void setSources(List<PropertySource> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return "Property [name=" + name + ", value=" + value + ", sources=" + sources + "]";
    }

}
