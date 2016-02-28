package nl.jk5.pumpkin.server.sql.obj;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.jk5.pumpkin.api.mappack.Mappack;
import nl.jk5.pumpkin.api.mappack.MappackTeam;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

@DatabaseTable(tableName = "mappack_team")
public class DatabaseMappackTeam implements MappackTeam {

    @DatabaseField(generatedId = true, unique = true)
    private int id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String color;

    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, columnName = "map_id")
    private DatabaseMappack mappack;

    @DatabaseField(columnName = "friendly_fire", defaultValue = "TRUE")
    private boolean friendlyFire;

    private TextColor textColor;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TextColor getColor() {
        if(this.textColor != null){
            return this.textColor;
        }
        switch (this.color) {
            case "aqua":
                this.textColor = TextColors.AQUA;
                break;
            case "black":
                this.textColor = TextColors.BLACK;
                break;
            case "blue":
                this.textColor = TextColors.BLUE;
                break;
            case "dark-aqua":
            case "cyan":
                this.textColor = TextColors.DARK_AQUA;
                break;
            case "dark-blue":
                this.textColor = TextColors.DARK_BLUE;
                break;
            case "dark-gray":
                this.textColor = TextColors.DARK_GRAY;
                break;
            case "dark-green":
                this.textColor = TextColors.DARK_GREEN;
                break;
            case "dark-purple":
                this.textColor = TextColors.DARK_PURPLE;
                break;
            case "dark-red":
                this.textColor = TextColors.DARK_RED;
                break;
            case "orange":
            case "gold":
                this.textColor = TextColors.GOLD;
                break;
            case "gray":
                this.textColor = TextColors.GRAY;
                break;
            case "green":
            case "lime":
                this.textColor = TextColors.GREEN;
                break;
            case "light-purple":
            case "magenta":
                this.textColor = TextColors.LIGHT_PURPLE;
                break;
            case "red":
                this.textColor = TextColors.RED;
                break;
            case "white":
                this.textColor = TextColors.WHITE;
                break;
            case "yellow":
                this.textColor = TextColors.YELLOW;
                break;
            default:
                this.textColor = TextColors.WHITE;
                break;
        }
        return this.textColor;
    }

    @Override
    public Mappack getMappack() {
        return this.mappack;
    }

    @Override
    public boolean isFriendlyFireEnabled() {
        return this.friendlyFire;
    }
}
