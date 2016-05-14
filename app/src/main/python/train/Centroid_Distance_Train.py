import csv

train_size = 5000

def train(training_set,training_size):
    print("Training")
    dataset = []
    center = [0]*12
    variance = [0]*12
    line_no = 0

    for filename in training_set:
        f = open(filename)
        f.readline()

        for line in f:
            # print(line)
            line_no +=1
            if (line_no>training_size):
                break
            line = line.strip()
            line = line.split('[')
            line = line[1:-1]
            idx = -1

            # print(line)
            dataline = []
            for l in line:
                # print(l)
                l = l.strip()
                l = l.split(',')
                l = l[:-1]
                # print(l)



                for j in range(0,len(l)) :
                    idx += 1
                    x = l[j]
                    x = x.strip()
                    if len(x)>0 :
                    # print(x)
                        if (x[len(x)-1] == ']'):
                            x = x[:-1]
                    # print(x)
                        y = float(x)
                        center[idx] += y
                        dataline.append(y)
                    else:
                        if j>=2:
                            # center[idx] += 0
                            dataline = dataline+[0,0,0]
                            idx +=2
                        else:
                            dataline.append(0)
            dataset.append(dataline)

    #TODO: Split into two functions
    # print(center)
    # print(len(dataset))
    # for c in center:
    #     c = c / float(len(dataset))

    for i in range(0, len(center)):
        center[i] = center[i]/float(len(dataset))


    print(center)
        #TODO : Normalise
    # for x in dataset:
    #     for i in range(0,len(x)):
    #         x[i] -= center[i]


        # for x in dataset:
        #     for i in range(0,len(x)):
        #         variance[i] +=

    max_dist = 0

    for x in dataset:
        dist = 0 ;
        for i in range(0, len(x)):
            dist += (x[i]-center[i])*(x[i]-center[i])
            if dist>max_dist:
                max_dist = dist

    max_dist = 1.00*max_dist

    # print(dataset)
    return center,max_dist

# train(['Rahul\Ra_1.csv'],5000)