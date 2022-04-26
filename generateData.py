import random
import os
import sys



# generate 15 Mb of data and save it to a file named data.txt
def generateData(megabytes):
    # delete file named data.txt if it exists
    if os.path.exists("data.txt"):
        os.remove("data.txt")

    with open('data.txt', 'x') as f:
        for i in range(340000 * megabytes):
            f.write(str(random.randint(0, 100)) + '\n')


if __name__ == "__main__":
    generateData(int(sys.argv[1]))
    print("File size: " + str(os.path.getsize("data.txt") / 1024 / 1024) + " MB")

