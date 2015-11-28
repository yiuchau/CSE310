/**
 * Represents a DNS record
 */
public class Record {

    public enum Type {
        A, NS
    }

    private String name;
    private Type type;
//    private String value;

    public Record(String name, Type type) {
        this.name = name;
        this.type = type;
//        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Record) {
            Record record = (Record) obj;

            return this.name.equals(record.getName()) && this.type == record.getType();
        }

        return false;
    }
}

