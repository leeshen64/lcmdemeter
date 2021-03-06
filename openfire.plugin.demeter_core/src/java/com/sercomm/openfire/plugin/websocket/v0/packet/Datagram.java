package com.sercomm.openfire.plugin.websocket.v0.packet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import com.sercomm.commons.util.Json;
import com.sercomm.commons.util.XStringUtil;
import com.sercomm.openfire.plugin.component.DeviceComponent;
import com.sercomm.openfire.plugin.exception.DemeterException;

public class Datagram implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    private Type type;
    private final List<Object> arguments = new ArrayList<Object>();
    
    public Datagram()
    {
    }
    
    public String getId()
    {
        return this.id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public Type getType()
    {
        return this.type;
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }

    public List<Object> getArguments()
    {
        return this.arguments;
    }

    @Override
    public String toString()
    {
        return Json.build(this);
    }
    
    public static Datagram parse(String text)
    throws Throwable
    {
        return Json.mapper().readValue(
            text, 
            Datagram.class);
    }
    
    public static Datagram make(
            String id,
            Type type)
    {
        Datagram packet = new Datagram();
        packet.id = id;
        packet.type = type;
        
        return packet;
    }
    
    public static IQ convertDatagramToIQ(JID from, JID to, Datagram datagram)
    {
        final IQ requestIQ = new IQ();
        requestIQ.setID(datagram.getId());
        requestIQ.setType(org.xmpp.packet.IQ.Type.set);
        requestIQ.setFrom(from);
        requestIQ.setTo(to);
        
        Element elmRoot = requestIQ.setChildElement(
            DeviceComponent.ELM_ROOT, 
            DeviceComponent.NAMESPACE);
        elmRoot.addAttribute(DeviceComponent.ATT_TYPE, datagram.getType().name());
        
        Element elmArguments = elmRoot.addElement(DeviceComponent.ELM_ARGUMENTS);
        elmArguments.setText(Base64.getEncoder().encodeToString(Json.build(datagram.getArguments()).getBytes()));

        return requestIQ;
    }
    
    public static Datagram convertIQToDatagram(IQ stanza)
    throws DemeterException
    {
        Element element = stanza.getChildElement();
        if(null == element ||
           0 != DeviceComponent.ELM_ROOT.compareTo(element.getName()))
        {
            throw new DemeterException("NO ROOT ELEMENT AVAILABLE: " + stanza.toXML());
        }

        String idString = stanza.getID();
        String typeString = element.attributeValue("type");
        if(XStringUtil.isBlank(idString) ||
           XStringUtil.isBlank(typeString))
        {
            throw new DemeterException("ID/TYPE IS BLANK: " + stanza.toXML());
        }

        Datagram datagram = Datagram.make(idString, Type.fromString(typeString));
        try
        {
            Element elmArguments = element.element(DeviceComponent.ELM_ARGUMENTS);
            String resultJSON = new String(Base64.getDecoder().decode(elmArguments.getText()));
            
            List<Object> arguments = Json.mapper().readValue(
                resultJSON,
                Json.mapper().getTypeFactory().constructCollectionType(
                    ArrayList.class,
                    Object.class));  
            
            for(Object argument : arguments)
            {
                datagram.getArguments().add(argument);
            }
        }
        catch(Throwable t)
        {
            throw new DemeterException("INVALID ARGUMENTS FORMAT: " + stanza.toXML());
        }

        return datagram;
    }
}
