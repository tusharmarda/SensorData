import Train

def test(filename, orig_label, center, max_dist, test_size):
    f = open(filename)
    f.readline()

    dataset = []
    line_no = -1

    tested_lines = 0

    #PARSING
    for line in f:
        # print(line)
        tested_lines += 1
        if tested_lines>test_size:
            break
        line_no += 1
        line = line.strip()
        line = line.split('[')
        line = line[1:-1]
        # print(line)
        dataline = []
        for l in line:
            l = l.strip()
            l = l.split(',')
            l = l[:-1]
            # print(l)
            for j in range(0, len(l)):


                x = l[j]
                x = x.strip()
                if len(x) >0 :

                    # x = x[:-1]
                    if (x[len(x) - 1] == ']'):
                        x = x[:-1]
                    # print(x)
                    # print("Line number " , line_no)
                    # print(x)
                    y = float(x)
                    # center[j] += y
                    dataline.append(y)
                else:
                    if j>=3:
                        dataline += [0,0,0]
        dataset.append(dataline)

    #TODO : Preprocess

    positive = 0
    negative = 0

        #REAL TESTING
    for i in range(0,len(dataset)):
        dist = 0
        for j in range(0,len(dataset[i])):
            dist += (dataset[i][j] - center[j])*(dataset[i][j] - center[j])
        if dist<max_dist:
            positive += 1
        else:
            negative += 1

    accuracy = 0

    if orig_label:
        accuracy = positive/(positive+negative)
    else:
        accuracy = negative/(negative+positive)

    return accuracy

cen, maxd = Train.train(['Aakash\Aakash_1.csv'],1000000)
# cen, maxd = Train.train(['Rahul\Ra_2.csv'],1000000)

acc = test('Rahul\Ra_2.csv', False, cen, maxd,100000 )

# acc = test('Aakash\Aakash_2.csv', True, cen, maxd,100000 )
print(acc)