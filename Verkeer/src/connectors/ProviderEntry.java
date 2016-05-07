package connectors;

public class ProviderEntry {

    private int id;
    private String name;

    /**
     *
     * @param id database-id of the provider
     * @param name name of the provider
     */
    public ProviderEntry(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     *
     */
    public ProviderEntry() {
    }

    /**
     * Returns the databaseId of the provider
     *
     * @return integer with databaseId
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name of the provider
     *
     * @return String with name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object other){
        if(other == null) return false;
        if(other == this) return true;
        if(!(other instanceof ProviderEntry)) return false;
        ProviderEntry otherProviderEntry = (ProviderEntry) other;
        if( otherProviderEntry.getName().equals(name) ) return true;
        else return false;
    }

}
