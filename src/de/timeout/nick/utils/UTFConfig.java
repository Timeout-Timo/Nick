package de.timeout.nick.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class UTFConfig extends YamlConfiguration {
	
	public UTFConfig(File file) {
		try {
			load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void save(File file) throws IOException {
		Validate.notNull(file, "File can't be null");
		Files.createParentDirs(file);
		String data = this.saveToString();
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
		
		try {
			writer.write(data);
		} finally {writer.close();}
	}
	
	@Override
	public String saveToString() {
		try {
			Field optionField = Reflections.getField(getClass(), "yamlOptions");
			Field representerField = Reflections.getField(getClass(), "yamlRepresenter");
			Field yamlField = Reflections.getField(getClass(), "yaml");
			
			optionField.setAccessible(true);
			representerField.setAccessible(true);
			yamlField.setAccessible(true);
			
			DumperOptions yamlOptions = (DumperOptions) optionField.get(this);
			Representer yamlRepresenter = (Representer) representerField.get(this);
			Yaml yaml = (Yaml) yamlField.get(this);
			DumperOptions.FlowStyle flow = DumperOptions.FlowStyle.BLOCK;
			
			yamlOptions.setIndent(this.options().indent());
			yamlOptions.setDefaultFlowStyle(flow);
			yamlOptions.setAllowUnicode(true);
			yamlRepresenter.setDefaultFlowStyle(flow);
			
			String header = this.buildHeader();
			String dump = yaml.dump(this.getValues(false));
			
			if(dump.equals("{}\n"))dump = "";
			return header + dump;
		} catch (Exception e) {e.printStackTrace();}
		return "Error: Cannot be saved to String";
	}
	
	@Override
	public void load(File file) throws IOException, InvalidConfigurationException {
		Validate.notNull(file, "File can't be null");
		this.load(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
	}
	
	public static class Reflections {
	    
	    public static Field modifiers = getField( Field.class, "modifiers" );

	    {
	        setAccessible( true, modifiers );
	    }

	    public Class< ? > getNMSClass( String name ) {
	        String version = Bukkit.getServer().getClass().getPackage().getName().split( "\\." )[ 3 ];
	        try {
	            return Class.forName( "net.minecraft.server." + version + "." + name );
	        } catch ( Exception e ) {
	            return null;
	        }
	    }

	    public Class< ? > getBukkitClass( String name ) {
	        try {
	            String version = Bukkit.getServer().getClass().getPackage().getName().split( "\\." )[ 3 ];
	            return Class.forName( "org.bukkit.craftbukkit." + version + "." + name );
	        } catch( Exception ex ) {
	            return null;
	        }
	    }

	    public void sendPacket( Player to, Object packet ) {
	        try {
	            Object playerHandle = to.getClass().getMethod( "getHandle" ).invoke( to );
	            Object playerConnection = playerHandle.getClass().getField( "playerConnection" ).get( playerHandle );
	            playerConnection.getClass().getMethod( "sendPacket", getNMSClass( "Packet" ) ).invoke( playerConnection, packet );
	        } catch ( Exception e ) {
	            e.printStackTrace();
	        }
	    }

	    public void setField( Object change, String name, Object to ) {
	        try {
	            Field field = getField( change.getClass(), name );
	            setAccessible( true, field );
	            field.set( change, to );
	            setAccessible( false, field);
	        } catch( Exception ex ) {
	            ex.printStackTrace();
	        }
	    }

	    public static void setAccessible( boolean state, Field... fields ) {
	        try {
	            for( Field field : fields ) {
	                field.setAccessible( state );
	                if( Modifier.isFinal( field.getModifiers() ) ) {
	                	field.setAccessible(true);
	                    modifiers.set( field, field.getModifiers() & ~Modifier.FINAL );
	                }
	            }
	        } catch( Exception ex ) {
	            ex.printStackTrace();
	        }
	    }

	    public static Field getField( Class< ? > clazz, String name ) {
	        Field field = null;

	        for( Field f : getFields( clazz ) ) {
	            if( !f.getName().equals( name ) )
	                continue;

	            field = f;
	            break;
	        }

	        return field;
	    }

	    public static List< Field > getFields( Class< ? > clazz ) {
	    	List< Field > buf = new ArrayList<>();

	    	do {
	    		try {
	    			for( Field f : clazz.getDeclaredFields() )
	  	        	buf.add( f );
	    		} catch( Exception ex ) {}
	    	} while( ( clazz = clazz.getSuperclass() ) != null );

	    	return buf;
	    }
	    
	    public String getVersion() {
    		String ver = Bukkit.getServer().getClass().getPackage().getName().split( "\\." )[ 3 ];
    		return ver;
	    }
	}
}
