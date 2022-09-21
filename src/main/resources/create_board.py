import graph_tool as GT
import graph_tool.topology
# import xml.etree.ElementTree as ET
import lxml.etree as ET

number_of_nodes = 10
# Define my list of edges in advance
edge_list = [
      (1, 2, 'taxi', True)
    , (1, 3, 'bus', True)
    , (2, 8, 'boat', False)
    , (5, 8, 'underground', True)
    , (5, 3, 'taxi', True)
    , (7, 9, 'boat', False)
    , (7, 4, 'taxi', True)
]

g = GT.Graph(directed=False)
g.add_vertex(number_of_nodes)
ep_transportation = g.new_ep("string")
ep_public = g.new_ep("bool")
g.add_edge_list(edge_list, eprops=[ep_transportation, ep_public])

xml_board_root = ET.Element('boardPositions')
xml_hiders_root = ET.Element('distances')
xml_seekers_root = ET.Element('distances')

# To produce the Seekers distance map we filter by only public edges
g.set_edge_filter(ep_public)
sd_seekers = graph_tool.topology.shortest_distance(g)
# To produce the Hiders distance map we remove the filter to include all edges
g.set_edge_filter(None)
sd_hiders = graph_tool.topology.shortest_distance(g)

for s in g.vertices():
    if not s.out_degree():
        continue  # Skip unused nodes (typically 0)
    boardPosition = ET.SubElement(xml_board_root, 'boardPosition', {'id': str(s)})

    for e in s.all_edges():
        assert(e.source() == s)
        action = ET.SubElement(boardPosition, 'action')
        ET.SubElement(action, 'destination').text = str(e.target())
        ET.SubElement(action, 'transportation').text = ep_transportation[e]

    from_seekers = None  # We will initialize these later, when needed.
    from_hiders = None

    for t in g.vertices(): # TODO: we can speed this up by not iterating through all vertices if we know that we should start at s anyway
        if (t > s):
            dist_seekers = sd_seekers[s][t]
            dist_hiders = sd_hiders[s][t]

            if dist_seekers <= number_of_nodes:
                if from_seekers is None:
                    from_seekers = ET.SubElement(xml_seekers_root, 'from', {'id': str(s)})
                to_seekers = ET.SubElement(from_seekers, 'to', {'id': str(t)})
                ET.SubElement(to_seekers, 'distance').text = str(dist_seekers)

            if dist_hiders <= number_of_nodes:
                if from_hiders is None:
                    from_hiders = ET.SubElement(
                        xml_hiders_root, 'from', {'id': str(s)})
                to_hiders = ET.SubElement(from_hiders, 'to', {'id': str(t)})
                ET.SubElement(to_hiders, 'distance').text = str(dist_hiders)

xml_board = ET.ElementTree(xml_board_root)
xml_board.write('board_file.xml', encoding='UTF-8',
                xml_declaration=True, pretty_print=True)

xml_seekers = ET.ElementTree(xml_seekers_root)
xml_seekers.write('seekers_distances_file.xml', encoding='UTF-8',
                  xml_declaration=True, pretty_print=True)

xml_hiders = ET.ElementTree(xml_hiders_root)
xml_hiders.write('hiders_distances_file.xml', encoding='UTF-8',
                 xml_declaration=True, pretty_print=True)
