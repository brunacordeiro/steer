<?xml version="1.0" encoding="UTF-8"?>
<simconf>
  <project EXPORT="discard">[APPS_DIR]/mrm</project>
  <project EXPORT="discard">[APPS_DIR]/mspsim</project>
  <project EXPORT="discard">[APPS_DIR]/avrora</project>
  <project EXPORT="discard">[APPS_DIR]/serial_socket</project>
  <project EXPORT="discard">[APPS_DIR]/collect-view</project>
  <project EXPORT="discard">[APPS_DIR]/powertracker</project>
  <simulation>
    <title>NovoTesteJISA-1</title>
    <speedlimit>1.0</speedlimit>
    <randomseed>123456</randomseed>
    <motedelay_us>1000000</motedelay_us>
    <radiomedium>
      org.contikios.cooja.radiomediums.UDGM
      <transmitting_range>50.0</transmitting_range>
      <interference_range>100.0</interference_range>
      <success_ratio_tx>1.0</success_ratio_tx>
      <success_ratio_rx>1.0</success_ratio_rx>
    </radiomedium>
    <events>
      <logoutput>40000</logoutput>
    </events>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype1</identifier>
      <description>Application Mote Type #apptype1</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Sink</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype2</identifier>
      <description>Application Mote Type #apptype2</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Mote</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype3</identifier>
      <description>Application Mote Type #apptype3</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Sink</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype4</identifier>
      <description>Application Mote Type #apptype4</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Mote</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype5</identifier>
      <description>Application Mote Type #apptype5</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Sink</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype6</identifier>
      <description>Application Mote Type #apptype6</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Sink</moteclass>
    </motetype>
    <motetype>
      org.contikios.cooja.motes.ImportAppMoteType
      <identifier>apptype7</identifier>
      <description>Application Mote Type #apptype7</description>
      <motepath>[COOJA_DIR]/examples/sdn-wise_java_IDN/build</motepath>
      <moteclass>com.github.sdnwiselab.sdnwise.cooja.Mote</moteclass>
    </motetype>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>1</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>60.12375351993375</x>
        <y>31.964269196474667</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype6</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>2</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>58.47392099789349</x>
        <y>9.522360257306488</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype7</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>3</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>83.09733681428412</x>
        <y>52.29907348165914</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype7</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>4</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>90.43485387021595</x>
        <y>20.935829163898642</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype7</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>5</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>28.46360410864766</x>
        <y>20.096743403750242</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype7</motetype_identifier>
    </mote>
    <mote>
      <interface_config>
        org.contikios.cooja.motes.AbstractApplicationMoteType$SimpleMoteID
        <id>6</id>
      </interface_config>
      <interface_config>
        org.contikios.cooja.interfaces.Position
        <x>44.55615561453894</x>
        <y>51.68371371218196</y>
        <z>0.0</z>
      </interface_config>
      <motetype_identifier>apptype7</motetype_identifier>
    </mote>
  </simulation>
  <plugin>
    org.contikios.cooja.plugins.SimControl
    <width>280</width>
    <z>0</z>
    <height>160</height>
    <location_x>28</location_x>
    <location_y>402</location_y>
  </plugin>
  <plugin>
    org.contikios.cooja.plugins.LogListener
    <plugin_config>
      <filter />
      <formatted_time />
      <coloring />
    </plugin_config>
    <width>950</width>
    <z>1</z>
    <height>558</height>
    <location_x>400</location_x>
    <location_y>0</location_y>
  </plugin>
  <plugin>
    org.contikios.cooja.plugins.Visualizer
    <plugin_config>
      <moterelations>true</moterelations>
      <skin>org.contikios.cooja.plugins.skins.IDVisualizerSkin</skin>
      <skin>org.contikios.cooja.plugins.skins.MoteTypeVisualizerSkin</skin>
      <skin>org.contikios.cooja.plugins.skins.TrafficVisualizerSkin</skin>
      <viewport>5.691788919609912 0.0 0.0 5.691788919609912 -144.37246284140025 -2.9372757749214675</viewport>
    </plugin_config>
    <width>400</width>
    <z>2</z>
    <height>400</height>
    <location_x>1</location_x>
    <location_y>1</location_y>
  </plugin>
</simconf>

