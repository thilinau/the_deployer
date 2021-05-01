package it.bridge.database;

public class Pare
{
    String regex;
    String replacement;

    public Pare( String regex, String replacement )
    {
        this.regex = regex;
        this.replacement = replacement;
    }

    public String getReplacement()
    {
        return replacement;
    }

    public void setReplacement( String replacement )
    {
        this.replacement = replacement;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex( String regex )
    {
        this.regex = regex;
    }
}
