package netflix.adminresources.resources.model;

public class PropertySource {
    private String sourceName;
    private String value;

    public PropertySource() {
    }

    public PropertySource(String sourceName, String value) {
        this.sourceName = sourceName;
        this.value = value;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PropertySource [sourceName=" + sourceName + ", value=" + value + "]";
    }
}
