package ecomod.common.tiles.compat;

import ecomod.api.EcomodStuff;
import ecomod.common.pollution.config.PollutionEffectsConfig;
import ecomod.common.tiles.TileAnalyzer;
import ecomod.common.utils.EMUtils;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Analyzable;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class TileAnalyzerAdapter extends TileEntityEnvironment implements Analyzable{

	public TileAnalyzerAdapter() {
		super();
		node = Network.newNode(this, Visibility.Network).withComponent("pollution_analyzer").create();
	}
	
	public boolean attachedToAnalyzer()
	{
		return EMUtils.get1NearbyTileEntity(EMUtils.resloc("tile_analyzer"), getWorld(), getPos()) != null;
	}
	
	public TileAnalyzer getAnalyzer()
	{
		return (TileAnalyzer)EMUtils.get1NearbyTileEntity(EMUtils.resloc("tile_analyzer"), getWorld(), getPos());
	}


	@Override
	public Node[] onAnalyze(EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		player.sendMessage(new TextComponentString(attachedToAnalyzer() ? "Attached to Pollution Analyzer" : "Not attached to Pollution Analyzer"));
		return new Node[]{this.node()};
	}
	
	//Callback OC methods
	
	@Callback
    public Object[] is_attached(Context context, Arguments args) throws Exception
	{
		return new Object[]{attachedToAnalyzer()};
	}
	
	@Callback
    public Object[] analyze(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			return new Object[]{getAnalyzer().analyze() != null};
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
	}
	
	@Callback
    public Object[] get_pollution_data(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			TileAnalyzer ta = getAnalyzer();
			if(ta.pollution != null)
				return new Object[]{ta.pollution.toString()};
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
		
		return new Object[]{};
	}
	
	@Callback
    public Object[] get_pollution_data_separately(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			TileAnalyzer ta = getAnalyzer();
			if(ta.pollution != null)
				return new Object[]{ta.pollution.getAirPollution(), ta.pollution.getWaterPollution(), ta.pollution.getSoilPollution()};
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
		
		return new Object[]{};
	}
	
	@Callback
    public Object[] get_time_analyzed(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			TileAnalyzer ta = getAnalyzer();
			return new Object[]{ta.last_analyzed};
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
	}
	
	DateFormat DATE_FORMAT = new SimpleDateFormat();
	
	@Callback
    public Object[] get_time_analyzed_as_date(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			TileAnalyzer ta = getAnalyzer();
			if(ta.last_analyzed != -1)
				return new Object[]{DATE_FORMAT.format(new Date(ta.last_analyzed))};
			else
				return new Object[]{-1};
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
	}
	
	@Callback
	public Object[] get_pollution_effects(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			TileAnalyzer ta = getAnalyzer();
			if(ta.pollution != null)
			{
				List<String> effs = new ArrayList<>();
				for(String s : EcomodStuff.pollution_effects.keySet())
				{
					if(PollutionEffectsConfig.isEffectActive(s, ta.pollution))
					{
						effs.add(s);
					}
				}
				return effs.toArray(new String[effs.size()]);
			}
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
		
		return new Object[]{};
	}
	
	@Callback
    public Object[] get_energy(Context context, Arguments args) throws Exception
	{
		if(attachedToAnalyzer())
		{
			TileAnalyzer ta = getAnalyzer();
			return new Object[]{ta.getEnergyStored()};
		}
		else
		{
			throw new Exception("The analyzer is not attached!");
		}
	}
}
