package fi.dy.masa.litematica.materials;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;

/**
 * Keyword based "group" support for the material list, based on the
 * grouping feature from Tomyto536/tomysLists.
 * <p>
 * Each grouping is stored as a single string in the form {@code keyword|excluded1,excluded2},
 * where the excluded part is optional. An item belongs to a grouping if its display name
 * contains the keyword (case insensitive) and does not contain any of the excluded terms.
 */
public class MaterialListGroupUtils
{
    /**
     * The default set of groupings, ported from the default {@code tomyslistconfig.txt}
     * entries written by {@code FileUtils.writeDefaultGroupings()} in Tomyto536/tomysLists.
     */
    public static final ImmutableList<String> DEFAULT_GROUPINGS = ImmutableList.of(
            "oak|leave,sapling, pale, dark",
            "spruce|leave,sapling",
            "birch|leave,sapling",
            "jungle|leave,sapling",
            "acacia|leave,sapling",
            "dark oak|leave,sapling",
            "mangrove|leave,propagule",
            "cherry|leave,sapling",
            "bamboo|leave,sapling",
            "crimson|fungus, nylium, roots",
            "warped|fungus, roots, nylium",
            "pale|leave,sapling",
            "leave|",
            "sapling|",
            "stone|redstone, sand, glowstone, end, black, cutter, lode, dripstone, cobble",
            "cobblestone|",
            "andesite|",
            "diorite|",
            "granite|",
            "deepslate|",
            "blackstone|",
            "basalt|",
            "tuff|",
            "dripstone|",
            "mud",
            "prismarine",
            "resin",
            "quartz",
            "brick|stone, deepslate, mud, nether, tuff, prismarine, resin, quartz",
            "nether brick|",
            "sand|soul, stone",
            "sandstone|",
            "concrete|",
            "terracotta|",
            "glass|",
            "wool|",
            "carpet|moss",
            "copper|",
            "lantern",
            "candle"
    );

    /**
     * Reorders the material list's entries into clusters based on the configured groupings,
     * and switches the list to display in that order. Groups are ordered in the same order
     * they are defined in, entries within a group (and ungrouped entries) are ordered by name.
     * This is a one-shot action: the assigned order sticks until the entries are re-grouped,
     * the material list is regenerated, or a different sort criteria is selected.
     */
    public static void applyGrouping(MaterialListBase materialList)
    {
        LinkedHashMap<String, List<String>> groupings = parseGroupings(Configs.Generic.MATERIAL_LIST_GROUPS.getStrings());
        List<MaterialListEntry> entries = new ArrayList<>(materialList.getMaterialsFiltered(false));

        entries.sort((entry1, entry2) -> {
            int group1 = getGroupIndex(entry1.getStack().getHoverName().getString(), groupings);
            int group2 = getGroupIndex(entry2.getStack().getHoverName().getString(), groupings);
            int normalized1 = group1 < 0 ? Integer.MAX_VALUE : group1;
            int normalized2 = group2 < 0 ? Integer.MAX_VALUE : group2;

            if (normalized1 != normalized2)
            {
                return Integer.compare(normalized1, normalized2);
            }

            return entry1.getStack().getHoverName().getString().compareTo(entry2.getStack().getHoverName().getString());
        });

        for (int i = 0; i < entries.size(); ++i)
        {
            entries.get(i).setGroupOrder(i);
        }

        materialList.setGroupedSort();
    }

    /**
     * Parses the raw {@code keyword|excluded1,excluded2} strings from the config
     * into an ordered map of keyword -&gt; excluded terms.
     */
    public static LinkedHashMap<String, List<String>> parseGroupings(List<String> rawGroupings)
    {
        LinkedHashMap<String, List<String>> groupings = new LinkedHashMap<>();

        for (String line : rawGroupings)
        {
            String trimmed = line.trim();

            if (trimmed.isEmpty())
            {
                continue;
            }

            String[] parts = trimmed.split("\\|", 2);
            String keyword = parts[0].trim();

            if (keyword.isEmpty())
            {
                continue;
            }

            List<String> excluded = new ArrayList<>();

            if (parts.length > 1 && parts[1].isBlank() == false)
            {
                for (String term : parts[1].split(","))
                {
                    String trimmedTerm = term.trim();

                    if (trimmedTerm.isEmpty() == false)
                    {
                        excluded.add(trimmedTerm);
                    }
                }
            }

            groupings.putIfAbsent(keyword, excluded);
        }

        return groupings;
    }

    /**
     * Returns the index of the first grouping (in definition order) that the given
     * item name matches, or -1 if the item does not match any grouping.
     */
    public static int getGroupIndex(String itemName, Map<String, List<String>> groupings)
    {
        String lowerName = itemName.toLowerCase(Locale.ROOT);
        int index = 0;

        for (Map.Entry<String, List<String>> entry : groupings.entrySet())
        {
            if (lowerName.contains(entry.getKey().toLowerCase(Locale.ROOT)))
            {
                boolean isExcluded = false;

                for (String excludedTerm : entry.getValue())
                {
                    if (lowerName.contains(excludedTerm.toLowerCase(Locale.ROOT)))
                    {
                        isExcluded = true;
                        break;
                    }
                }

                if (isExcluded == false)
                {
                    return index;
                }
            }

            ++index;
        }

        return -1;
    }
}
