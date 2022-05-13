package hu.nomarcub.teenywar.model.events

import hu.nomarcub.teenywar.model.buidingblock.Packet

class ReceiveEvent(val packet: Packet, val wasCaptured: Boolean)