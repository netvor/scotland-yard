lines = open('fantom-board.csv','r').read().splitlines()
for l in lines:
    s,black,green,red,blue = l.split('\t')
    for t in black.split(): print(f"    , ({s}, {t}, 'bus', True)")
    for t in green.split(): print(f"    , ({s}, {t}, 'taxi', True)")
    for t in red.split(): print(f"    , ({s}, {t}, 'underground', True)")
    for t in blue.split(): print(f"    , ({s}, {t}, 'boat', True)")
