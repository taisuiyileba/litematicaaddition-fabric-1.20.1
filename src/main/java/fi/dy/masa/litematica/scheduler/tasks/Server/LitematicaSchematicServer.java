package fi.dy.masa.litematica.scheduler.tasks.Server;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.SchematicMetadata;
import fi.dy.masa.litematica.util.FileType;

import java.io.File;

public class LitematicaSchematicServer {
    private final LitematicaSchematic schematic;
    public LitematicaSchematicServer(File file, FileType schematicType){
        this.schematic = new LitematicaSchematic(file, schematicType);
    }
    public LitematicaSchematic getLitematicaSchematic() {
        return schematic;
    }
    public boolean readFromFile()
    {
        return getLitematicaSchematic().readFromFile();
    }
    public SchematicMetadata getMetadata()
    {
        return getLitematicaSchematic().getMetadata();
    }
}
