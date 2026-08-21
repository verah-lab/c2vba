# tls-ifacetlsoip

## Table Of Contents

* [About](#About)
* [Quersulzbach](#Important)

## About

This is the main functionality in order to operate the protocal WANCOM over IP.

In order to be fully functional, an instance of Osi7Cfg has to be provided as a bean.
Two (new) modules are used here to fit into certain system environments:  
tls-ifacesysconkafka: This is the system connection in the sense of the iface-framework
tls-ifaceroutingcfg: an adapter to configurations. Especially the config services used these days (2019 ...) for Austria and the German State Hessen.

## Important

Important notes for the understanding of the transport process follow.

### Message type TeleSToSend

This type is used when data has to be sent to TLS resources

    message TeleSToSend {
        google.protobuf.Timestamp time_sent = 1;    // the time the telegram was sent by the generating process
        int32 iface_key = 2;                        // ifacekey of process who shall transport this telegram
        int32 real_address = 3;                     // the real address (node number ~Knotennummer) of remaining data
        int32 flags = 4;                            // legacy compatibility
        bytes osi7_tel = 5;                         // the TLS-Sammeltelegramm
        string iid = 6;                             // uuid
    }

The meaning of most of the fields is obvious or documented inline.

iface_key selects a certain process, that transports the telegrams. A group of destiantions may be assigned to an iface. The configuration  has to assign a iface_key to the next communication partner of an iface. All destinations below/behind that node will be reached by the given iface_key.

real_address is the the node number the telegram is meant to be sent to. Since ther is also a node number in the data contained in osi7_tel, this may be confusing. At least I was. The node number in osi7_tel gives you the node number where the transported data belongs to. While real_address ist the final destination of the transport.
As long as you send data down the TLS hierarchy those node number are identical since the target is the road side station.

flags looks as if it is not used by now.

iid may carry a (universal) unique id for th message.

### Message type TeleSReceived

This type is used, when a telegram frm outside TLS resources is received

    message TeleSReceived {
        google.protobuf.Timestamp time_rcvd = 1;    // the time the telegram was received
        int32 iface_key = 2;                        // ifacekey of corresponding iface process who received this telegram
        int32 real_address = 3;                     // the real address (node number ~Knotennummer) of remaining data
        int32 flags = 4;                            // legacy compatibility
        bytes tls_s_tel = 5;                        // TLS-Sammeltelegramm
        string iid = 6;                             // uuid
    }

In addition to the preceeding paragraph there is to be mentioned, that real_addres ist the node number where we received the data from.

Again the node number contained in tls_s_tel holds the origin of the data.