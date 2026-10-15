package dev.inscribed.knowledge;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
public record GuidePayload(boolean open,GuideSnapshot snapshot,long sequence,boolean complete) implements CustomPacketPayload {
    public GuidePayload(boolean open,GuideSnapshot snapshot){this(open,snapshot,0,true);}
    public static final Type<GuidePayload> TYPE=new Type<>(ResourceLocation.parse("inscribed:guide_snapshot"));
    private static int count(RegistryFriendlyByteBuf b,int max){int n=b.readVarInt();if(n<0||n>max)throw new IllegalArgumentException("Guide collection exceeds limit");return n;}
    public static final StreamCodec<RegistryFriendlyByteBuf,GuidePayload> STREAM_CODEC=new StreamCodec<>(){
        public GuidePayload decode(RegistryFriendlyByteBuf b){
            boolean open=b.readBoolean();long sequence=b.readLong();boolean complete=b.readBoolean();long generation=b.readLong();var es=new ArrayList<GuideSnapshot.KnownEnchantment>();
            for(int i=0,n=count(b,4096);i<n;i++)es.add(new GuideSnapshot.KnownEnchantment(b.readResourceLocation(),ComponentSerialization.STREAM_CODEC.decode(b),ComponentSerialization.STREAM_CODEC.decode(b),b.readVarInt()));
            var rs=new ArrayList<GuideSnapshot.Recipe>();
            for(int i=0,n=count(b,8192);i<n;i++){
                var id=b.readResourceLocation();var ench=b.readResourceLocation();int level=b.readVarInt();boolean extraction=b.readBoolean();
                var name=ComponentSerialization.STREAM_CODEC.decode(b);var search=ComponentSerialization.STREAM_CODEC.decode(b);
                int power=b.readVarInt(),ticks=b.readVarInt();double chance=b.readDouble();int books=b.readVarInt();boolean exact=b.readBoolean();
                var costs=new ArrayList<GuideSnapshot.Cost>();
                for(int j=0,m=count(b,64);j<m;j++){int amount=b.readVarInt();var alts=new ArrayList<ItemStack>();for(int k=0,l=count(b,64);k<l;k++)alts.add(ItemStack.STREAM_CODEC.decode(b));costs.add(new GuideSnapshot.Cost(alts,amount));}
                var targets=new ArrayList<ItemStack>();for(int j=0,m=count(b,12);j<m;j++)targets.add(ItemStack.STREAM_CODEC.decode(b));
                rs.add(new GuideSnapshot.Recipe(id,ench,level,extraction,name,search,power,ticks,chance,books,exact,costs,targets));
            }
            return new GuidePayload(open,new GuideSnapshot(generation,es,rs),sequence,complete);
        }
        public void encode(RegistryFriendlyByteBuf b,GuidePayload p){
            b.writeBoolean(p.open);b.writeLong(p.sequence);b.writeBoolean(p.complete);b.writeLong(p.snapshot.generation());b.writeVarInt(p.snapshot.enchantments().size());
            for(var e:p.snapshot.enchantments()){b.writeResourceLocation(e.id());ComponentSerialization.STREAM_CODEC.encode(b,e.name());ComponentSerialization.STREAM_CODEC.encode(b,e.searchable());b.writeVarInt(e.maxLevel());}
            b.writeVarInt(p.snapshot.recipes().size());
            for(var r:p.snapshot.recipes()){
                b.writeResourceLocation(r.id());b.writeResourceLocation(r.enchantment());b.writeVarInt(r.level());b.writeBoolean(r.extraction());
                ComponentSerialization.STREAM_CODEC.encode(b,r.name());ComponentSerialization.STREAM_CODEC.encode(b,r.searchable());
                b.writeVarInt(r.power());b.writeVarInt(r.ticks());b.writeDouble(r.chance());b.writeVarInt(r.emptyBooks());b.writeBoolean(r.exact());b.writeVarInt(r.costs().size());
                for(var c:r.costs()){b.writeVarInt(c.count());b.writeVarInt(c.alternatives().size());for(var item:c.alternatives())ItemStack.STREAM_CODEC.encode(b,item);}
                b.writeVarInt(r.targets().size());for(var item:r.targets())ItemStack.STREAM_CODEC.encode(b,item);
            }
        }
    };
    public Type<GuidePayload> type(){return TYPE;}
}
