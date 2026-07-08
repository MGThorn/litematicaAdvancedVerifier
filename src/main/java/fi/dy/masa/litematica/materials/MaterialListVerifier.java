package fi.dy.masa.litematica.materials;

import java.util.List;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.BlockMismatch;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;

public class MaterialListVerifier extends MaterialListBase
{
    private final SchematicVerifier verifier;
    private final String placementName;

    public MaterialListVerifier(SchematicVerifier verifier, String placementName)
    {
        super();
        this.verifier = verifier;
        this.placementName = placementName;
        this.reCreateMaterialList();
    }

    @Override
    public String getName()
    {
        return this.placementName;
    }

    @Override
    public String getTitle()
    {
        return "Material List: " + this.placementName;
    }

    @Override
    public void reCreateMaterialList()
    {
        Object2IntOpenHashMap<BlockState> countsTotal = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<BlockState> countsMissing = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<BlockState> countsMismatch = new Object2IntOpenHashMap<>();

        for (BlockMismatch mismatch : this.verifier.getMismatchOverviewFor(MismatchType.MISSING))
        {
            countsTotal.addTo(mismatch.stateExpected, mismatch.count);
            countsMissing.addTo(mismatch.stateExpected, mismatch.count);
        }

        for (BlockMismatch mismatch : this.verifier.getMismatchOverviewFor(MismatchType.WRONG_BLOCK))
        {
            countsTotal.addTo(mismatch.stateExpected, mismatch.count);
            countsMissing.addTo(mismatch.stateExpected, mismatch.count);
            countsMismatch.addTo(mismatch.stateExpected, mismatch.count);
        }

        List<MaterialListEntry> entries = MaterialListUtils.getMaterialList(
                countsTotal, countsMissing, countsMismatch,
                Minecraft.getInstance().player);

        this.setMaterialListEntries(entries);
    }
}
