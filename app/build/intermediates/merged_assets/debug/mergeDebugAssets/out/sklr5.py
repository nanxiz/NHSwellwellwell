import os
import os.path as path
import pandas as pd
import numpy as np
import pickle
#import matplotlib  
#matplotlib.use('TkAgg')   
#import matplotlib.pyplot as plt 
from scipy import stats
import tensorflow as tf
from sklearn import preprocessing, metrics
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from tensorflow.python.tools import freeze_graph
from tensorflow.python.tools import optimize_for_inference_lib


EXPORT_DIR = './model'

learning_rate = 0.1
training_epochs = 250
batch_size = 250
display_step = 1
MODEL_NAME = 'lrmodel'
    

columns = ['score', 'steps', 'calls', 'calltime', 'messages', 'media', 'camera', 'social', 'browser'] #NB add browser as an 8th?
dt = pd.read_csv('welldata.csv', header=0, dtype=float)
dt.head()
dt.info()


npArray = np.array(dt)
print(dt.shape)

#data splitting and preprocessing time
xvals = npArray[:,1:].astype(float)
yvals = npArray[:,0].astype(int)

print(str(yvals[2,]))
print(str(yvals[57,]))

print ("X dimensions:", xvals.shape)
print ("y dimensions:", yvals.shape)

#Data scaling

x_norm = preprocessing.scale(xvals)


x_norm.reshape(1, x_norm.shape[0], x_norm.shape[1])
print ("New X dimensions:", x_norm.shape)
print("90000 steps: " + str(x_norm[10,0]))
print("19000 steps: " + str(x_norm[6,0]))
print("0 thingies: " + str(x_norm[6,5]))
print("7 thingies: " + str(x_norm[5,5]))


XTrain, XTest, yTrain, yTest = train_test_split(x_norm, yvals, random_state=1)

log_reg = LogisticRegression(class_weight='balanced')
log_reg.fit(XTrain, yTrain)
yPred = log_reg.predict(XTest)

print (metrics.classification_report(yTest, yPred))
print ("Overall Accuracy:", round(metrics.accuracy_score(yTest, yPred),2))

#-----one-hot encoding -------------
yTrain_resize = np.zeros((len(yTrain),11))
yTrain_resize[np.arange(len(yTrain)),yTrain] = 1
print ("y dimensions:", yTrain_resize.shape)

yTrain = yTrain_resize

yTest_resize = np.zeros((len(yTest),11))
yTest_resize[np.arange(len(yTest)),yTest] = 1
print ("y dimensions:", yTest_resize.shape)

yTest = yTest_resize

print("train dimensions:", XTrain.shape)
print("test dimensions:", XTest.shape)
print("train label dimensions:", yTrain.shape)
print("test label dimensions:", yTest.shape)


#-----building the logistic regression model---

# placeholders

def create_LR_model(inputs):
    W = tf.Variable(tf.random_normal([8, 11], mean=0.0, stddev=0.05))
    b = tf.Variable(tf.zeros([11]))
    
    X = tf.reshape(inputs, [-1, 8])

    return (tf.matmul(X, W) + b)

tf.reset_default_graph()

X = tf.placeholder(tf.float32, [None, 8], name="input")
Y = tf.placeholder(tf.float32, [None, 11])

out = create_LR_model(X)
pred = tf.nn.softmax(out, name="output")

# Minimize error using cross entropy
cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits = out, labels = Y))
# Gradient Descent
optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(cost)

train_count = len(XTrain)

#defining optimizer and accuracy
correct_prediction = tf.equal(tf.argmax(pred, 1), tf.argmax(Y, 1))

accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))


#----Training the model------------------------------------------
saver = tf.train.Saver()

history = dict(train_loss=[], 
                train_acc=[], 
                test_loss=[], 
                test_acc=[])

sess=tf.InteractiveSession()
sess.run(tf.global_variables_initializer())

train_count = len(XTrain)

for i in range(1, training_epochs + 1):
    for start, end in zip(range(0, train_count, batch_size),
                          range(batch_size, train_count + 1,batch_size)):
        sess.run(optimizer, feed_dict={X: XTrain[start:end],
                                        Y: yTrain[start:end]})

    _, acc_train, loss_train = sess.run([pred, accuracy, cost], feed_dict={
                                                X: XTrain, Y: yTrain})

    _, acc_test, loss_test = sess.run([pred, accuracy, cost], feed_dict={
                                                X: XTest, Y: yTest})

    history['train_loss'].append(loss_train)
    history['train_acc'].append(acc_train)
    history['test_loss'].append(loss_test)
    history['test_acc'].append(acc_test)

    if i != 1 and i % 10 != 0:
        continue

    print(f'epoch: {i} test accuracy: {acc_test} loss: {loss_test}')

    
predictions, acc_final, loss_final = sess.run([pred, accuracy, cost], feed_dict={X: XTest, Y: yTest})

tf.train.write_graph(sess.graph_def, 'checkpoint', 'lrmodel.pbtxt', True)  

saver.save(sess, save_path = "./checkpoint/lrmodel.ckpt")

print()
print(f'final results: accuracy: {acc_final} loss: {loss_final}')

#-----Storing model to disk-----------------------------------------
input_node_name = 'input'
output_node_name = 'output'

if not path.exists('checkpoint'):
        os.mkdir('checkpoint')

input_graph_path = 'checkpoint/' + MODEL_NAME+'.pbtxt'
checkpoint_path = './checkpoint/' +MODEL_NAME+'.ckpt'
restore_op_name = "save/restore_all"
filename_tensor_name = "save/Const:0"
output_frozen_graph_name = 'frozen_'+MODEL_NAME+'.pb'

freeze_graph.freeze_graph(input_graph_path, input_saver="",
                          input_binary=False, input_checkpoint=checkpoint_path, 
                          output_node_names=output_node_name, restore_op_name="save/restore_all",
                          filename_tensor_name="save/Const:0", 
                          output_graph=output_frozen_graph_name, clear_devices=True, initializer_nodes="")

print("graph saved!")

    


#-----Storing model to disk-----------------------------------------
